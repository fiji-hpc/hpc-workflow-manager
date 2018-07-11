package kozusznik.haas_experiment;

import java.io.InterruptedIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.HaaSFileTransfer;
import cz.it4i.fiji.haas_java_client.JobInfo;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.UploadingFileData;

public class Routines {

	public static final Logger log = LoggerFactory.getLogger(kozusznik.haas_experiment.Routines.class);
	
	public static long startBDS(HaaSClient client) throws InterruptedException {
		long jobId =  429;/*client.createJob(new
		  JobSettingsBuilder().jobName("TestOutRedirect").templateId(4l)
		  .walltimeLimit(600).clusterNodeType(7l).build(), Collections.emptyList());
	 */
	
		JobInfo info = client.obtainJobInfo(jobId);
		log.info("JobId :" + jobId + ", state - " + info.getState());
		if (info.getState() != JobState.Running) {
			try (HaaSFileTransfer transfer = client.startFileTransfer(jobId)) {
				transfer.upload(new UploadingFileData("run-bds"));
			} catch (InterruptedIOException e) {
				log.error(e.getMessage(), e);
			}
			client.submitJob(jobId);
		}
		JobState state;
		while((state = client.obtainJobInfo(jobId).getState()) != JobState.Running) {
			log.info("state - " + state);
			Thread.sleep(3000);
		}
		return jobId;
	}

}
