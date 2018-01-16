package cz.it4i.fiji.haas;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import cz.it4i.fiji.haas.JobManager.JobInfo;
import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.JobState;
import net.imagej.updater.util.Progress;

public class BenchmarkJobManager {
	private static final String CONFIG_YAML = "config.yaml";
	private JobManager jobManager;
	private Progress progress;
	

	public BenchmarkJobManager(Path workDirectory, Progress progress) throws IOException {
		jobManager = new JobManager(workDirectory, TestingConstants.getSettings(2, 6));
		this.progress = progress;
	}

	public JobInfo createJob() throws IOException {
		JobInfo jobInfo = jobManager.createJob(progress);
		jobInfo.storeDataInWorkdirectory(getUploadingFile());
		return jobInfo;
	}

	public void startJob(JobInfo jobInfo) {
		jobInfo.uploadFilesByName(() -> Arrays.asList(CONFIG_YAML).stream());
		jobInfo.submit();
	}

	public JobState getState(long jobId) {
		return jobManager.getState(jobId);
	}

	private HaaSClient.UploadingFile getUploadingFile() {
		return new UploadingFileFromResource("", CONFIG_YAML);
	}

	public Collection<JobInfo> getJobs() throws IOException {
		return jobManager.getJobs(progress);
	}

}
