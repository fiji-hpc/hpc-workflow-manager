package cz.it4i.fiji.haas;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.HaaSClientSettings;
import cz.it4i.fiji.haas_java_client.JobSettings;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;

public class JobManager implements Closeable {

	interface JobManager4Job {
		boolean deleteJob(Job job);

		boolean canUpload(Job j, Path p);
	}

	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.JobManager.class);

	private static final BiPredicate<Job, Path> DUMMY_UPLOAD_FILTER = (X, Y) -> true;

	private final Path workDirectory;

	private Collection<Job> jobs;

	private HaaSClient haasClient;

	private final HaaSClientSettings settings;

	private BiPredicate<Job, Path> uploadFilter = DUMMY_UPLOAD_FILTER;

	private final JobManager4Job remover = new JobManager4Job() {

		@Override
		public boolean deleteJob(Job job) {
			//do not remove Job from server according to issue #1125
			job.updateInfo();
			if(job.getState() == JobState.Running) {
				job.cancelJob();
			}
			return jobs.remove(job);
		}

		@Override
		public boolean canUpload(Job j, Path p) {
			return uploadFilter.test(j, p);
		}
	};

	public JobManager(Path workDirectory, HaaSClientSettings settings) {
		this.workDirectory = workDirectory;
		this.settings = settings;
	}

	public Job createJob(JobSettings jobSettings ,Function<Path, Path> inputDirectoryProvider, Function<Path, Path> outputDirectoryProvider)
			throws IOException {
		Job result;
		initJobsIfNecessary();
		jobs.add(result = new Job(remover, jobSettings, workDirectory, this::getHaasClient,
				inputDirectoryProvider, outputDirectoryProvider));
		return result;
	}

	public Collection<Job> getJobs() {
		initJobsIfNecessary();
		return Collections.unmodifiableCollection(jobs);
	}

	public void checkConnection() {
		getHaasClient().checkConnection();
	}

	public void setUploadFilter(BiPredicate<Job, Path> filter) {
		uploadFilter = filter != null ? filter : DUMMY_UPLOAD_FILTER;
	}

	@Override
	synchronized public void close() {
		if(jobs != null) {
			jobs.forEach(job -> job.close());
		}
	}

	private HaaSClient getHaasClient() {
		if (haasClient == null) {
			haasClient = new HaaSClient(settings);
		}
		return haasClient;
	}

	synchronized private void initJobsIfNecessary() {
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
