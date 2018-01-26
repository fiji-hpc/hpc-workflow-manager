package cz.it4i.fiji.haas;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import cz.it4i.fiji.haas.JobManager.JobSynchronizableFile;
import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;
import cz.it4i.fiji.haas_java_client.JobInfo;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import net.imagej.updater.util.Progress;

public class Job {

	private static final String JOB_NAME = "job.name";

	public static boolean isJobPath(Path p) {
		return isValidPath(p);
	}

	private static String JOB_INFO_FILE = ".jobinfo";

	@Parameter
	private LogService log;

	private Path jobDir;

	private Supplier<HaaSClient> haasClientSupplier;

	// private JobState state;
	//private Boolean needsDownload;
	private JobInfo jobInfo;
	private Long jobId;

	
	private PropertyHolder propertyHolder;

	public Job(String name, Path basePath, Supplier<HaaSClient> haasClientSupplier) throws IOException {
		this(haasClientSupplier);
		HaaSClient client = this.haasClientSupplier.get();
		long id = client.createJob(name, Collections.emptyList());
		jobDir = basePath.resolve("" + id);
		propertyHolder = new PropertyHolder(jobDir.resolve(JOB_INFO_FILE));
		setName(name);
		Files.createDirectory(jobDir);
	}

	public void setName(String name) {
		setProperty(JOB_NAME, name);
	}

	public Job(Path p, Supplier<HaaSClient> haasClientSupplier) {
		this(haasClientSupplier);
		jobDir = p;
		propertyHolder = new PropertyHolder(jobDir.resolve(JOB_INFO_FILE));
	}

	public void uploadFiles(Iterable<UploadingFile> files, Progress notifier) {
		HaaSClient client = this.haasClientSupplier.get();

		client.uploadFiles(jobId, files, new P_ProgressNotifierAdapter(notifier));
	}

	public void uploadFilesByName(Iterable<String> files, Progress notifier) {
		Iterable<UploadingFile> uploadingFiles = StreamSupport.stream(files.spliterator(), false)
				.map((String name) -> HaaSClient.getUploadingFile(jobDir.resolve(name))).collect(Collectors.toList());
		uploadFiles(uploadingFiles, notifier);
	}

	public void submit() {
		HaaSClient client = this.haasClientSupplier.get();
		client.submitJob(jobId);
	}

	private Job(Supplier<HaaSClient> haasClientSupplier) {
		this.haasClientSupplier = haasClientSupplier;
	}

	

	synchronized public long getJobId() {
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
		haasClientSupplier.get().download(getJobId(), jobDir, predicate, new P_ProgressNotifierAdapter(notifier));
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
		return haasClientSupplier.get().downloadPartsOfJobFiles(jobId, taskFileOffset).stream().map(f -> f.getContent())
				.collect(Collectors.toList());
	}

	public InputStream openLocalFile(String name) throws IOException {
		return Files.newInputStream(jobDir.resolve(name));
	}

	public void setProperty(String name, String value) {
		propertyHolder.setValue(name, value);
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
	
	private JobInfo getJobInfo() {
		if (jobInfo == null) {
			updateJobInfo();
		}
		return jobInfo;
	}

	private void updateJobInfo() {
		jobInfo = haasClientSupplier.get().obtainJobInfo(getJobId());
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
			super();
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

}
