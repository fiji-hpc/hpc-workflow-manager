package cz.it4i.fiji.haas;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cz.it4i.fiji.haas.JobManager.JobInfo;
import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.JobState;
import net.imagej.updater.util.Progress;

public class BenchmarkJobManager {
	private JobManager jobManager;
	private Progress progress;
	private Map<JobInfo, Path> uploadedFiles = new HashMap<>();

	public BenchmarkJobManager(Path workDirectory, Progress progress) throws IOException {
		jobManager = new JobManager(workDirectory, TestingConstants.getSettings(3, 6));
		this.progress = progress;
	}

	public JobInfo createJob() throws IOException {
		JobInfo jobInfo = jobManager.createJob(progress);
		Path file = jobInfo.storeDataInWorkdirectory(getUploadingFile());
		uploadedFiles.put(jobInfo, file);
		return jobInfo;
	}

	public void startJob(JobInfo jobInfo) {
		jobInfo.uploadFiles(Arrays.asList(HaaSClient.getUploadingFile(uploadedFiles.get(jobInfo))).stream());
		jobInfo.submit();
	}

	public JobState getState(long jobId) {
		return jobManager.getState(jobId);
	}

	private HaaSClient.UploadingFile getUploadingFile() {
		return new UploadingFileFromResource("", "config.yaml");
	}

}
