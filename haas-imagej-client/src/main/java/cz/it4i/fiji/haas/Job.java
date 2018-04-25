package cz.it4i.fiji.haas;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.JobManager.JobManager4Job;
import cz.it4i.fiji.haas.JobManager.JobSynchronizableFile;
import cz.it4i.fiji.haas.data_transfer.Synchronization;
import cz.it4i.fiji.haas_java_client.DummyProgressNotifier;
import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;
import cz.it4i.fiji.haas_java_client.HaaSFileTransfer;
import cz.it4i.fiji.haas_java_client.JobInfo;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import net.imagej.updater.util.Progress;

public class Job {

	private static final String JOB_NAME = "job.name";
	
	private static final String JOB_NEEDS_UPLOAD = "job.needs_upload";

	public static boolean isJobPath(Path p) {
		return isValidPath(p);
	}

	private static String JOB_INFO_FILE = ".jobinfo";

	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.Job.class);

	private Path jobDir;

	private Supplier<HaaSClient> haasClientSupplier;

	// private JobState state;
	// private Boolean needsDownload;
	private JobInfo jobInfo;
	private Long jobId;
	private PropertyHolder propertyHolder;
	private JobManager4Job jobManager;
	private Synchronization synchronization;

	public Job(JobManager4Job jobManager, String name, Path basePath, Supplier<HaaSClient> haasClientSupplier)
			throws IOException {
		this(jobManager, haasClientSupplier);
		HaaSClient client = getHaaSClient();
		long id = client.createJob(name, Collections.emptyList());
		jobDir = basePath.resolve("" + id);
		propertyHolder = new PropertyHolder(jobDir.resolve(JOB_INFO_FILE));
		Files.createDirectory(jobDir);
		setName(name);

	}

	public Job(JobManager4Job jobManager, Path jobDirectory, Supplier<HaaSClient> haasClientSupplier) {
		this(jobManager, haasClientSupplier);
		jobDir = jobDirectory;
		propertyHolder = new PropertyHolder(jobDir.resolve(JOB_INFO_FILE));
		resumeSynchronization();
	}

	private Job(JobManager4Job jobManager, Supplier<HaaSClient> haasClientSupplier) {
		this.haasClientSupplier = haasClientSupplier;
		this.jobManager = jobManager;
		try {
			this.synchronization = new Synchronization(()->haasClientSupplier.get().startFileTransfer(getId(), new DummyProgressNotifier()), jobDir, Executors.newFixedThreadPool(2), ()->  {
				setProperty(JOB_NEEDS_UPLOAD, false);
			});
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public void startUploadData()  {
		setProperty(JOB_INFO_FILE, true);
		try {
			this.synchronization.startUpload();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	public void stopUploadData()  {
		setProperty(JOB_INFO_FILE, false);
		try {
			this.synchronization.stopUpload();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	public void uploadFile(String file, Progress notifier) {
		Iterable<UploadingFile> uploadingFiles = Arrays.asList(file).stream()
				.map((String name) -> HaaSClient.getUploadingFile(jobDir.resolve(name))).collect(Collectors.toList());
		uploadFiles(uploadingFiles, notifier);
	}

	public void submit() {
		HaaSClient client = getHaaSClient();
		client.submitJob(jobId);
	}

	synchronized public long getId() {
		if (jobId == null) {
			jobId = getJobId(jobDir);
		}
		return jobId;
	}

	public void download(Progress notifier) {
		download(x -> true, notifier);
	}

	public Path storeDataInWorkdirectory(UploadingFile uploadingFile) throws IOException {
		Path result;
		try (InputStream is = uploadingFile.getInputStream()) {
			Files.copy(is, result = jobDir.resolve(uploadingFile.getName()));
		}
		return result;
	}

	synchronized public void download(Predicate<String> predicate, Progress notifier) {
		try (HaaSFileTransfer fileTransfer = getHaaSClient().startFileTransfer(jobId,
				new P_ProgressNotifierAdapter(notifier))) {
			fileTransfer.download(
					getHaaSClient().getChangedFiles(jobId).stream().filter(predicate).collect(Collectors.toList()),
					jobDir);
		}
	}

	public JobState getState() {
		return getJobInfo().getState();
	}

	public Calendar getStartTime() {
		return jobInfo.getStartTime();
	}

	public Calendar getCreationTime() {
		return jobInfo.getCreationTime();
	}

	public Calendar getEndTime() {
		return jobInfo.getEndTime();
	}

	public List<String> getOutput(Iterable<JobSynchronizableFile> output) {
		HaaSClient.SynchronizableFiles taskFileOffset = new HaaSClient.SynchronizableFiles();
		long taskId = (Long) getJobInfo().getTasks().toArray()[0];
		output.forEach(file -> taskFileOffset.addFile(taskId, file.getType(), file.getOffset()));
		return getHaaSClient().downloadPartsOfJobFiles(jobId, taskFileOffset).stream().map(f -> f.getContent())
				.collect(Collectors.toList());
	}

	public InputStream openLocalFile(String name) throws IOException {
		return Files.newInputStream(jobDir.resolve(name));
	}

	public synchronized void setProperty(String name, String value) {
		propertyHolder.setValue(name, value);
	}

	public void setProperty(String jobNeedsUpload, boolean b) {
		propertyHolder.setValue(jobNeedsUpload, "" + b);
	}

	public String getProperty(String name) {
		return propertyHolder.getValue(name);
	}

	public void updateInfo() {
		updateJobInfo();
	}

	public Path getDirectory() {
		return jobDir;
	}

	public boolean remove() {
		boolean result;
		if ((result = jobManager.remove(this)) && Files.isDirectory(jobDir)) {
			List<Path> pathsToDelete;
			try {
				pathsToDelete = Files.walk(jobDir).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
				for (Path path : pathsToDelete) {
					Files.deleteIfExists(path);
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}

		}
		return result;
	}

	private synchronized void resumeSynchronization() {
		if(Boolean.parseBoolean(getProperty(JOB_NEEDS_UPLOAD))) {
			synchronization.resumeUpload();
		}
	}

	private void uploadFiles(Iterable<UploadingFile> files, Progress notifier) {
		HaaSClient client = getHaaSClient();
		try (HaaSFileTransfer transfer = client.startFileTransfer(getId(), new P_ProgressNotifierAdapter(notifier))) {
			transfer.upload(files);
		}
	}

	private void setName(String name) {
		setProperty(JOB_NAME, name);
	}

	private HaaSClient getHaaSClient() {
		return this.haasClientSupplier.get();
	}

	private JobInfo getJobInfo() {
		if (jobInfo == null) {
			updateJobInfo();
		}
		return jobInfo;
	}

	private void updateJobInfo() {
		jobInfo = getHaaSClient().obtainJobInfo(getId());
	}

	private static boolean isValidPath(Path path) {

		try {
			getJobId(path);
		} catch (NumberFormatException e) {
			return false;
		}
		return Files.isRegularFile(path.resolve(JOB_INFO_FILE));
	}

	private static long getJobId(Path path) {
		return Long.parseLong(path.getFileName().toString());
	}

	private class P_ProgressNotifierAdapter implements ProgressNotifier {
		private Progress progress;

		public P_ProgressNotifierAdapter(Progress progress) {
			this.progress = progress;
		}

		public void setTitle(String title) {
			progress.setTitle(title);
		}

		public void setCount(int count, int total) {
			progress.setCount(count, total);
		}

		public void addItem(Object item) {
			progress.addItem(item);
		}

		public void setItemCount(int count, int total) {
			progress.setItemCount(count, total);
		}

		public void itemDone(Object item) {
			progress.itemDone(item);
		}

		public void done() {
			progress.done();
		}

	}

	public Collection<String> getChangedFiles() {
		return getHaaSClient().getChangedFiles(getId());
	}

	public void cancelJob() {
		getHaaSClient().cancelJob(jobId);
	}

	public List<Long> getFileSizes(List<String> names) {

		try (HaaSFileTransfer transfer = getHaaSClient().startFileTransfer(getId(), new DummyProgressNotifier())) {
			return transfer.obtainSize(names);
		}
	}

	public List<String> getFileContents(List<String> logs) {
		try (HaaSFileTransfer transfer = getHaaSClient().startFileTransfer(getId(), new DummyProgressNotifier())) {
			return transfer.getContent(logs);
		}
	}

}
