package cz.it4i.fiji.haas;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cz.it4i.fiji.haas.JobManager.JobInfo;
import cz.it4i.fiji.haas.JobManager.JobSynchronizableFile;
import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.JobState;
import net.imagej.updater.util.Progress;

public class BenchmarkJobManager {
	private static final String CONFIG_YAML = "config.yaml";
	private JobManager jobManager;
	private Progress progress;
	private Map<Long,JobInfo> jobs = new HashMap<>();

	public BenchmarkJobManager(Path workDirectory, Progress progress) throws IOException {
		jobManager = new JobManager(workDirectory, TestingConstants.getSettings(2, 6));
		this.progress = progress;
	}

	public long createJob() throws IOException {
		JobInfo jobInfo = jobManager.createJob(progress);
		jobInfo.storeDataInWorkdirectory(getUploadingFile());
		return indexJob(jobInfo);
	}

	public void startJob(long jobId) {
		JobInfo jobInfo = jobs.get(jobId);
		jobInfo.uploadFilesByName(() -> Arrays.asList(CONFIG_YAML).stream());
		jobInfo.submit();
	}

	
	public Collection<Long> getJobs() throws IOException {
		return jobManager.getJobs(progress).stream().map(this::indexJob).collect(Collectors.toList());
	}
	
	public JobState getState(long jobId) {
		return jobs.get(jobId).getState();
	}

	public void downloadData(long jobId) {
		// TODO Auto-generated method stub
		
		
		
	}
	public Iterable<String> getOutput(long jobId, List<JobSynchronizableFile> files) {
		return jobs.get(jobId).getOutput(files);
	}
	
	private HaaSClient.UploadingFile getUploadingFile() {
		return new UploadingFileFromResource("", CONFIG_YAML);
	}

	private long indexJob(JobInfo jobInfo) {
		jobs.put(jobInfo.getId(), jobInfo);
		return jobInfo.getId();
	}

	

	

	

}
