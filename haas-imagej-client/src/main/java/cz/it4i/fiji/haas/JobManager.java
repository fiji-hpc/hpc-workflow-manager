package cz.it4i.fiji.haas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.Settings;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;

public class JobManager {

	interface JobManager4Job {
		boolean remove(Job job);
	}
	
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.JobManager.class);

	private Path workDirectory;

	private Collection<Job> jobs;

	private HaaSClient haasClient;

	private Settings settings;

	private JobManager4Job remover = new JobManager4Job() {
		
		@Override
		public boolean remove(Job job) {
			return jobs.remove(job);
		}
	};
	
	public JobManager(Path workDirectory, Settings settings) {
		this.workDirectory = workDirectory;
		this.settings = settings;
	}

	public Job createJob() throws IOException {
		Job job;
		if (jobs == null) {
			jobs = new LinkedList<>();
		}
		jobs.add(job = new Job(remover, settings.getJobName(), workDirectory, this::getHaasClient));
		return job;
	}


	public Collection<Job> getJobs() {
		if (jobs == null) {
			jobs = new LinkedList<>();
			try {
				Files.list(this.workDirectory).filter(p -> Files.isDirectory(p) && Job.isJobPath(p)).forEach(p -> {
						jobs.add(new Job(remover, p, this::getHaasClient));
					
				});
			} catch (IOException e) {
				log.error(e.getMessage(),e);
			}
		}
		return Collections.unmodifiableCollection(jobs);
	}

//	public void downloadJob(Long id, Progress notifier) {
//		Iterator<Job> job = jobs.stream().filter(j -> j.getId() == id).iterator();
//		assert job.hasNext();
//		job.next().download(notifier);
//
//	}

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
