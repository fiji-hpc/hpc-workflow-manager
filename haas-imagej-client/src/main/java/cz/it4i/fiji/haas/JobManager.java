package cz.it4i.fiji.haas;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.Settings;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import javafx.beans.value.ObservableValueBase;
import net.imagej.updater.util.Progress;

public class JobManager {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.JobManager.class);

	private Path workDirectory;

	private Collection<Job> jobs;

	private HaaSClient haasClient;

	private Settings settings;

	public JobManager(Path workDirectory, Settings settings) {
		this.workDirectory = workDirectory;
		this.settings = settings;
	}

	public JobInfo createJob() throws IOException {
		Job job;
		if (jobs == null) {
			jobs = new LinkedList<>();
		}
		jobs.add(job = new Job(settings.getJobName(), workDirectory, this::getHaasClient));
		return new JobInfo(job) {
			@Override
			public JobState getState() {
				job.updateInfo();
				return super.getState();
			}
		};
	}

	public JobInfo startJob(Iterable<UploadingFile> files, Progress notifier) throws IOException {
		JobInfo result = createJob();
		result.uploadFiles(files, notifier);
		result.submit();
		return result;
	}

	public Iterable<JobInfo> getJobsNeedingDownload() {
		return () -> jobs.stream().filter(j -> j.needsDownload()).map(j -> new JobInfo(j)).iterator();
	}

	public Collection<JobInfo> getJobs() throws IOException {
		if (jobs == null) {
			jobs = new LinkedList<>();
			Files.list(this.workDirectory).filter(p -> Files.isDirectory(p) && Job.isJobPath(p)).forEach(p -> {
				try {
					jobs.add(new Job(p, this::getHaasClient));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		return jobs.stream().map(j -> new JobInfo(j)).collect(Collectors.toList());
	}

	public void downloadJob(Long id, Progress notifier) {
		Iterator<Job> job = jobs.stream().filter(j -> j.getJobId() == id).iterator();
		assert job.hasNext();
		job.next().download(notifier);

	}

	public JobState getState(long id) {
		return getHaasClient().obtainJobInfo(id).getState();
	}

	private HaaSClient getHaasClient() {
		if (haasClient == null) {
			haasClient = new HaaSClient(settings);
		}
		return haasClient;
	}

	public static class JobSynchronizableFile {
		private SynchronizableFileType type;
		private long offset;

		public JobSynchronizableFile(SynchronizableFileType type, long offset) {
			super();
			this.type = type;
			this.offset = offset;
		}

		public SynchronizableFileType getType() {
			return type;
		}

		public long getOffset() {
			return offset;
		}
	}

	public static class JobInfo extends ObservableValueBase<JobInfo> {

		private Job job;

		public JobInfo(Job job) {
			this.job = job;
		}

		public void uploadFiles(Iterable<UploadingFile> files, Progress notifier) {
			job.uploadFiles(files,notifier);
		}

		public void uploadFilesByName(Iterable<String> files, Progress notifier) {
			job.uploadFilesByName(files, notifier);
		}

		public void submit() {
			job.submit();
		}

		public Long getId() {
			return job.getJobId();
		}

		public JobState getState() {
			return job.getState();
		}

		public boolean needsDownload() {
			return job.needsDownload();
		}

		public String getCreationTime() {
			return getStringFromTimeSafely(job.getCreationTime());
		}

		public String getStartTime() {
			return getStringFromTimeSafely(job.getStartTime());
		}

		public String getEndTime() {
			return getStringFromTimeSafely(job.getEndTime());
		}

		public void downloadData(Progress notifier) {
			downloadData(x -> true, notifier);
		}

		public void downloadData(Predicate<String> predicate, Progress notifier) {
			job.download(predicate, notifier);
			fireValueChangedEvent();

		}

		public void waitForStart() {
			// TODO Auto-generated method stub

		}

		public void updateInfo() {
			job.updateInfo();
		}

		@Override
		public JobInfo getValue() {
			return this;
		}

		public Path storeDataInWorkdirectory(UploadingFile uploadingFile) throws IOException {
			return job.storeDataInWorkdirectory(uploadingFile);
		}

		public List<String> getOutput(Iterable<JobSynchronizableFile> files) {
			return job.getOutput(files);
		}

		private String getStringFromTimeSafely(Calendar time) {
			return time != null ? time.getTime().toString() : "N/A";
		}

		public InputStream openLocalFile(String name) throws IOException {
			return job.openLocalFile(name);
		}

		public void setProperty(String name, String value) throws IOException {
			job.setProperty(name, value);

		}

		public String getProperty(String name) throws IOException {
			return job.getProperty(name);
		}

		public Path getDirectory() {
			return job.getDirectory();
		}

	}

}
