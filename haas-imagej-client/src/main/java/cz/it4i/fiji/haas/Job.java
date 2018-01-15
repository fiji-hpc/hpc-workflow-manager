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
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

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

	private JobState state;
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

	public Job(String name, Path basePath, Supplier<HaaSClient> haasClientSupplier, Progress progress) throws IOException {
		this(haasClientSupplier);
		HaaSClient client = this.haasClientSupplier.get();
		long id = client.createJob(name, Collections.emptyList(),
				notifier = new P_ProgressNotifierAdapter(progress));
		jobDir = basePath.resolve("" + id);
		this.name = name;
		Files.createDirectory(jobDir);
		updateState();
	}

	public Job(Path p, Supplier<HaaSClient> haasClientSupplier) throws IOException {
		this(haasClientSupplier);
		jobDir = p;
		loadJobInfo();
	}

	public void uploadFiles(Stream<UploadingFile> files) {
		HaaSClient client = this.haasClientSupplier.get();
		client.uploadFiles(jobId, files, notifier);
	}

	public void submit() {
		HaaSClient client = this.haasClientSupplier.get();
		client.submitJob(jobId, notifier);
	}

	private Job(Supplier<HaaSClient> haasClientSupplier) {
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
		state = updateJobInfo().getState();
		if (needsDownload == null
				&& EnumSet.of(JobState.Failed, JobState.Finished, JobState.Canceled).contains(state)) {
			needsDownload = true;
		}
		saveJobinfo();
	}

	public void download() {
		download(dummy);
	}
	
	public Path storeDataInWorkdirectory(UploadingFile uploadingFile) throws IOException {
		Path result;
		try(InputStream is = uploadingFile.getInputStream()) {
			Files.copy(is, result = jobDir.resolve(uploadingFile.getName()));
		}
		return result;
	}

	synchronized public void download(Progress progress) {
		if (!needsDownload()) {
			throw new IllegalStateException("Job: " + getJobId() + " doesn't need download");
		}
		haasClientSupplier.get().download(getJobId(), jobDir, new P_ProgressNotifierAdapter(progress));
		needsDownload = false;
		try {
			saveJobinfo();
		} catch (IOException e) {
			log.error(e);
		}
	}

	public JobState getState() {
		return state;
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

	private synchronized void saveJobinfo() throws IOException {
		try (OutputStream ow = Files.newOutputStream(jobDir.resolve(JOB_INFO_FILE),
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
			Properties prop = new Properties();
			if (needsDownload != null) {
				prop.setProperty(JOB_HAS_DATA_TO_DOWNLOAD_PROPERTY, needsDownload.toString());
			}
			prop.setProperty(JOB_NAME, name);
			prop.store(ow, null);
		}
	}

	private synchronized void loadJobInfo() throws IOException {
		try (InputStream is = Files.newInputStream(jobDir.resolve(JOB_INFO_FILE))) {
			Properties prop = new Properties();
			prop.load(is);
			if (prop.containsKey(JOB_HAS_DATA_TO_DOWNLOAD_PROPERTY)) {
				needsDownload = Boolean.parseBoolean(prop.getProperty(JOB_HAS_DATA_TO_DOWNLOAD_PROPERTY));
				name = prop.getProperty(JOB_NAME);
			}
		}
	}

	private JobInfo updateJobInfo() {
		return jobInfo = haasClientSupplier.get().obtainJobInfo(getJobId());
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
