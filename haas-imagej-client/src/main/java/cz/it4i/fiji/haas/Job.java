package cz.it4i.fiji.haas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Properties;
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

	private static final String JOB_HAS_DATA_TO_DOWNLOAD_PROPERTY = "job.needDownload";

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
	private Boolean needsDownload;
	private JobInfo jobInfo;
	private Long jobId;
	private ProgressNotifier notifier;

	final private Progress dummy = new Progress() {

		@Override
		public void setTitle(String title) {
		}

		@Override
		public void setItemCount(int count, int total) {
		}

		@Override
		public void setCount(int count, int total) {
		}

		@Override
		public void itemDone(Object item) {
		}

		@Override
		public void done() {
		}

		@Override
		public void addItem(Object item) {
		}
	};

	private String name;

	public Job(String name, Path basePath, Supplier<HaaSClient> haasClientSupplier, Progress progress)
			throws IOException {
		this(haasClientSupplier, progress);
		HaaSClient client = this.haasClientSupplier.get();
		long id = client.createJob(name, Collections.emptyList(), notifier);
		jobDir = basePath.resolve("" + id);
		this.name = name;
		Files.createDirectory(jobDir);
		updateState();
	}

	public Job(Path p, Supplier<HaaSClient> haasClientSupplier, Progress progress) throws IOException {
		this(haasClientSupplier, progress);
		jobDir = p;
		loadJobInfo();
		updateState();
	}

	public void uploadFiles(Iterable<UploadingFile> files) {
		HaaSClient client = this.haasClientSupplier.get();
		client.uploadFiles(jobId, files, notifier);
	}

	public void uploadFilesByName(Iterable<String> files) {
		Iterable<UploadingFile> uploadingFiles = StreamSupport.stream(files.spliterator(), false)
				.map((String name) -> HaaSClient.getUploadingFile(jobDir.resolve(name))).collect(Collectors.toList());
		uploadFiles(uploadingFiles);
	}

	public void submit() {
		HaaSClient client = this.haasClientSupplier.get();
		client.submitJob(jobId, notifier);
	}

	private Job(Supplier<HaaSClient> haasClientSupplier, Progress progress) throws IOException {
		notifier = new P_ProgressNotifierAdapter(progress);
		this.haasClientSupplier = haasClientSupplier;
	}

	public boolean needsDownload() {
		return needsDownload != null && needsDownload;
	}

	synchronized public long getJobId() {
		if (jobId == null) {
			jobId = getJobId(jobDir);
		}
		return jobId;
	}

	synchronized public void updateState() throws IOException {
		if (needsDownload == null
				&& EnumSet.of(JobState.Failed, JobState.Finished, JobState.Canceled).contains(getState())) {
			needsDownload = true;
		}
		saveJobinfo();
	}

	public void download() {
		download(x -> true, dummy);
	}

	public Path storeDataInWorkdirectory(UploadingFile uploadingFile) throws IOException {
		Path result;
		try (InputStream is = uploadingFile.getInputStream()) {
			Files.copy(is, result = jobDir.resolve(uploadingFile.getName()));
		}
		return result;
	}

	synchronized public void download(Predicate<String> predicate, Progress progress) {
		if (!needsDownload()) {
			throw new IllegalStateException("Job: " + getJobId() + " doesn't need download");
		}
		haasClientSupplier.get().download(getJobId(), jobDir, predicate, new P_ProgressNotifierAdapter(progress));
		needsDownload = false;
		try {
			saveJobinfo();
		} catch (IOException e) {
			log.error(e);
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

	public Iterable<String> getOutput(Iterable<JobSynchronizableFile> output) {
		HaaSClient.SynchronizableFiles taskFileOffset = new HaaSClient.SynchronizableFiles();
		long taskId = (Long) getJobInfo().getTasks().toArray()[0];
		output.forEach(file -> taskFileOffset.addFile(taskId, file.getType(), file.getOffset()));
		return haasClientSupplier.get().downloadPartsOfJobFiles(jobId, taskFileOffset).stream().map(f -> f.getContent())
				.collect(Collectors.toList());
	}

	public InputStream openLocalFile(String name) throws IOException {
		return Files.newInputStream(jobDir.resolve(name));
	}

	public void setProperty(String name, String value) throws IOException {
		Properties prop = loadPropertiesIfExists();
		prop.setProperty(name, value);
		storeProperties(prop);
	}

	public String getProperty(String name) throws IOException {
		return loadPropertiesIfExists().getProperty(name);
	}

	private synchronized void saveJobinfo() throws IOException {
		Properties prop = loadPropertiesIfExists();
		if (needsDownload != null) {
			prop.setProperty(JOB_HAS_DATA_TO_DOWNLOAD_PROPERTY, needsDownload.toString());
		}
		prop.setProperty(JOB_NAME, name);
		storeProperties(prop);
	}

	private void storeProperties(Properties prop) throws IOException {
		try (OutputStream ow = Files.newOutputStream(jobDir.resolve(JOB_INFO_FILE),
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
			prop.store(ow, null);
		}
	}

	private synchronized void loadJobInfo() throws IOException {
		Properties prop = loadPropertiesIfExists();
		if (prop.containsKey(JOB_HAS_DATA_TO_DOWNLOAD_PROPERTY)) {
			needsDownload = Boolean.parseBoolean(prop.getProperty(JOB_HAS_DATA_TO_DOWNLOAD_PROPERTY));
		}
		name = prop.getProperty(JOB_NAME);
	}

	private Properties loadPropertiesIfExists() throws IOException {
		Properties prop = new Properties();
		if (Files.exists(jobDir.resolve(JOB_INFO_FILE))) {
			try (InputStream is = Files.newInputStream(jobDir.resolve(JOB_INFO_FILE))) {
				prop.load(is);
			}
		}
		return prop;
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
