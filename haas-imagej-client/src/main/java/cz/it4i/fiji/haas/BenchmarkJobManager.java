package cz.it4i.fiji.haas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;

import cz.it4i.fiji.haas.JobManager.JobInfo;
import cz.it4i.fiji.haas_java_client.JobState;
import net.imagej.updater.util.Progress;

public class BenchmarkJobManager {
	private JobManager jobManager;
	private Progress progress;
	private Path workDirectory;
	private static final String CONFIG_FOR_MODIFICATION = "not-set-config.yaml";
	private static final String CONFIG_MODIFIED = "config.yaml";
	
	public BenchmarkJobManager(Path workDirectory, Progress progress) throws IOException {
		this.workDirectory = workDirectory;
		jobManager = new JobManager(workDirectory, TestingConstants.getSettings(3, 6));
		this.progress = progress;
	}
	
	public JobInfo startJob() throws IOException {
		JobInfo jobInfo = jobManager.startJob( Collections.emptyList(), null);
		jobInfo.waitForStart();
		if(jobInfo.getState() != JobState.Running) {
			throw new IllegalStateException("start of job: " + jobInfo + " failed");
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		jobInfo.downloadFileData(CONFIG_FOR_MODIFICATION,os);
		byte[]data  = updateConfigFile(os.toByteArray());
		
		jobInfo.uploadFile (new ByteArrayInputStream(data),CONFIG_MODIFIED, data.length ,Instant.now().getEpochSecond());
		return jobInfo;
	}

	private byte[] updateConfigFile(byte[] data) throws IOException {
		return data;
		
	}

	
}
