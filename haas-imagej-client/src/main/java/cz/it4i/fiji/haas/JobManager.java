package cz.it4i.fiji.haas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.Settings;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
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

	public Job createJob() throws IOException {
		Job job;
		if (jobs == null) {
			jobs = new LinkedList<>();
		}
		jobs.add(job = new Job(settings.getJobName(), workDirectory, this::getHaasClient));
		return job;
	}

	public Job startJob(Iterable<UploadingFile> files, Progress notifier) throws IOException {
		Job result = createJob();
		result.uploadFiles(files, notifier);
		result.submit();
		return result;
	}

	public Collection<Job> getJobs() {
		if (jobs == null) {
			jobs = new LinkedList<>();
			try {
				Files.list(this.workDirectory).filter(p -> Files.isDirectory(p) && Job.isJobPath(p)).forEach(p -> {
						jobs.add(new Job(p, this::getHaasClient));
					
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return jobs.stream().collect(Collectors.toList());
	}

	public void downloadJob(Long id, Progress notifier) {
		Iterator<Job> job = jobs.stream().filter(j -> j.getId() == id).iterator();
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
}
