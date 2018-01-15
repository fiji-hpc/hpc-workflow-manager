package cz.it4i.fiji.haas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.Settings;
import javafx.beans.value.ObservableValueBase;
import net.imagej.updater.util.Progress;

public class JobManager {

	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.JobManager.class);

	private Path workDirectory;

	private Collection<Job> jobs = new LinkedList<>();

	private HaaSClient haasClient;

	private Settings settings;

	public JobManager(Path workDirectory, Settings settings) throws IOException {
		this.workDirectory = workDirectory;
		this.settings = settings;
		Files.list(this.workDirectory).filter(p -> Files.isDirectory(p) && Job.isJobPath(p)).forEach(p -> {
			try {
				jobs.add(new Job(p, this::getHaasClient));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

	}

	public JobInfo createJob(Progress progress) throws IOException {
		Job job;
		jobs.add(job = new Job(settings.getJobName(), workDirectory, this::getHaasClient, progress));
		return new JobInfo(job) {
			@Override
			public JobState getState() {
				try {
					job.updateState();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
				return super.getState();
			}
		};
	}

	public JobInfo startJob(Supplier<Stream<UploadingFile>> files, Progress progress) throws IOException {
		JobInfo result = createJob(progress);
		result.uploadFiles(files);
		result.submit();
		return result;
	}

	public Iterable<JobInfo> getJobsNeedingDownload() {
		return () -> jobs.stream().filter(j -> j.needsDownload()).map(j -> new JobInfo(j)).iterator();
	}

	public Collection<JobInfo> getJobs() {
		return jobs.stream().map(j -> new JobInfo(j)).collect(Collectors.toList());
	}

	public void downloadJob(Long id) {
		Iterator<Job> job = jobs.stream().filter(j -> j.getJobId() == id).iterator();
		assert job.hasNext();
		job.next().download();

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

	public static class JobInfo extends ObservableValueBase<JobInfo> {

		private Job job;

		public JobInfo(Job job) {
			this.job = job;
		}

		public void uploadFiles(Supplier<Stream<UploadingFile>> files) {
			job.uploadFiles(files);
		}
		
		public void uploadFilesByName(Supplier<Stream<String>> files) {
			job.uploadFilesByName(files);
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

		public void downloadData(Progress progress) {
			job.download(progress);
			fireValueChangedEvent();
		}

		public void waitForStart() {
			// TODO Auto-generated method stub

		}

		public void updateInfo() throws IOException {
			job.updateState();
		}

		@Override
		public JobInfo getValue() {
			return this;
		}

		private String getStringFromTimeSafely(Calendar time) {
			return time != null ? time.getTime().toString() : "N/A";
		}

		public Path storeDataInWorkdirectory(UploadingFile uploadingFile) throws IOException {
			return job.storeDataInWorkdirectory(uploadingFile);
		}

	}

}
