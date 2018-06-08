package cz.it4i.fiji.haas;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.Settings;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;

public class JobManager implements Closeable {

	interface JobManager4Job {
		boolean deleteJob(Job job);
	}

	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.JobManager.class);

	private final Path workDirectory;

	private Collection<Job> jobs;

	private HaaSClient haasClient;

	private final Settings settings;

	private final JobManager4Job remover = new JobManager4Job() {

		@Override
		public boolean deleteJob(Job job) {
			haasClient.deleteJob(job.getId());
			return jobs.remove(job);
		}
	};

	public JobManager(Path workDirectory, Settings settings) {
		this.workDirectory = workDirectory;
		this.settings = settings;
	}

	public Job createJob(Function<Path, Path> inputDirectoryProvider, Function<Path, Path> outputDirectoryProvider)
			throws IOException {
		Job result;
		initJobsIfNecessary();
		jobs.add(result = new Job(remover, settings.getJobName(), workDirectory, this::getHaasClient,
				inputDirectoryProvider, outputDirectoryProvider));
		return result;
	}

	public Collection<Job> getJobs() {
		initJobsIfNecessary();
		return Collections.unmodifiableCollection(jobs);
	}

	@Override
	public void close() {
		jobs.forEach(job -> job.close());
	}

	private HaaSClient getHaasClient() {
		if (haasClient == null) {
			haasClient = new HaaSClient(settings);
		}
		return haasClient;
	}

	private void initJobsIfNecessary() {
		if (jobs == null) {
			jobs = new LinkedList<>();
			try {
				Files.list(this.workDirectory).filter(p -> Files.isDirectory(p) && Job.isValidJobPath(p)).forEach(p -> {
					jobs.add(new Job(remover, p, this::getHaasClient));
				});
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public static class JobSynchronizableFile {
		private final SynchronizableFileType type;
		private final long offset;

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
