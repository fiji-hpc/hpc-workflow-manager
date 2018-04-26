package cz.it4i.fiji.haas_java_client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.StreamSupport;

import javax.xml.rpc.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient.SynchronizableFiles;
import cz.it4i.fiji.haas_java_client.proxy.JobFileContentExt;

public class TestHaaSJavaClientWithSPIM {

	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_java_client.TestHaaSJavaClientWithSPIM.class);
	
	public static void main(String[] args) throws ServiceException, IOException {
		HaaSClient client = new HaaSClient(TestingConstants.getSettings(2, 9600, 6l, "DD-17-31"));
		Path baseDir = Paths.get("/home/koz01/Work/vyzkumnik/fiji/work/aaa");

		long jobId = client.createJob( "TestOutRedirect",
				Collections.emptyList());
		
		try(HaaSFileTransfer tr = client.startFileTransfer(jobId, HaaSClient.DUMMY_TRANSFER_FILE_PROGRESS)) {
			StreamSupport.stream(getAllFiles(baseDir.resolve("spim-data")).spliterator(), false)
					.map(UploadingFileImpl::new).forEach(f -> tr.upload(f));
		}
		client.submitJob(jobId);
		
		Path workDir = baseDir.resolve("" + jobId);
		if (!Files.isDirectory(workDir)) {
			Files.createDirectories(workDir);
		}
		JobInfo info;
		boolean firstIteration = true;
		do {
			if (!firstIteration) {
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			info = client.obtainJobInfo(jobId);
			HaaSClient.SynchronizableFiles taskFileOffset = new HaaSClient.SynchronizableFiles();
			for (Long id : info.getTasks()) {
				addOffsetFilesForTask(id, taskFileOffset);
			}
			client.downloadPartsOfJobFiles(jobId, taskFileOffset).forEach(jfc -> showJFC(jfc));
			if (info.getState() == JobState.Finished) {
				try (HaaSFileTransfer fileTransfer = client.startFileTransfer(jobId, HaaSClient.DUMMY_TRANSFER_FILE_PROGRESS)) {
					client.getChangedFiles(jobId).forEach(file -> fileTransfer.download(file, workDir));
				}

			}
			log.info("JobId :" + jobId + ", state" + info.getState());
			firstIteration = false;
		} while (info.getState() != JobState.Canceled && info.getState() != JobState.Failed
				&& info.getState() != JobState.Finished);
	}

	private static Iterable<Path> getAllFiles(Path resolve) {

		return () -> {
			try {
				return Files.newDirectoryStream(resolve).iterator();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

	private static void addOffsetFilesForTask(Long taskId, SynchronizableFiles files) {
		files.addFile(taskId, SynchronizableFileType.ProgressFile, 0);
		files.addFile(taskId, SynchronizableFileType.StandardErrorFile, 0);
		files.addFile(taskId, SynchronizableFileType.StandardOutputFile, 0);
		files.addFile(taskId, SynchronizableFileType.LogFile, 0);
	}

	private static void showJFC(JobFileContentExt file) {
		log.info("File: " + file.getFileType() + ", " + file.getRelativePath());
		log.info("TaskInfoId: " + file.getSubmittedTaskInfoId());
		log.info("Offset: " + file.getOffset());
		log.info("Content: " + file.getContent());
	}

}
