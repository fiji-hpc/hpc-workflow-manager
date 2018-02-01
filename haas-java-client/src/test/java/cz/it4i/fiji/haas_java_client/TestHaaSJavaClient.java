package cz.it4i.fiji.haas_java_client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient.SynchronizableFiles;
import cz.it4i.fiji.haas_java_client.proxy.JobFileContentExt;

public class TestHaaSJavaClient {

	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_java_client.TestHaaSJavaClient.class);

	public static void main(String[] args) throws ServiceException, IOException {
		Map<String, String> params = new HashMap<>();
		params.put("inputParam", "someStringParam");
		Path baseDir = Paths.get("/home/koz01/aaa");
		HaaSClient client = new HaaSClient(TestingConstants.getSettings(1l, 600, 7l, "DD-17-31"));
		long jobId = client.start(Arrays.asList(Paths.get("/home/koz01/aaa/vecmath.jar")), "TestOutRedirect",
				params.entrySet());
		Path workDir = baseDir.resolve("" + jobId);
		if (!Files.isDirectory(workDir)) {
			Files.createDirectories(workDir);
		}
		JobInfo info;
		do {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			info = client.obtainJobInfo(jobId);
			HaaSClient.SynchronizableFiles taskFileOffset = new HaaSClient.SynchronizableFiles();
			for (Long id : info.getTasks()) {
				addOffsetFilesForTask(id, taskFileOffset);
			}
			client.downloadPartsOfJobFiles(jobId, taskFileOffset).forEach(jfc -> showJFC(jfc));
			if (info.getState() == JobState.Finished) {
				try (HaaSFileTransfer fileTransfer = client.startFileTransfer(jobId, HaaSClient.DUMMY_NOTIFIER)) {
					fileTransfer.download(client.getChangedFiles(jobId), workDir);
				}
			}
			log.info("JobId :" + jobId + ", state" + info.getState());
		} while (info.getState() != JobState.Canceled && info.getState() != JobState.Failed
				&& info.getState() != JobState.Finished);
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
