
package cz.it4i.fiji.haas;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.hpc_client.JobState;
import cz.it4i.fiji.hpc_client.SynchronizableFileType;

public class JobManager implements Closeable {

	interface JobManager4Job {

		boolean deleteJob(Job job);

		boolean canUpload(Job j, Path p);
	}

	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas.JobManager.class);

	private static final BiPredicate<Job, Path> DUMMY_UPLOAD_FILTER = (x,
		y) -> true;

	private final Path workDirectory;

	private Collection<Job> jobs;


	private BiPredicate<Job, Path> uploadFilter = DUMMY_UPLOAD_FILTER;

	private final JobManager4Job remover = new JobManager4Job() {

		@Override
		public boolean deleteJob(Job job) {
			// do not remove Job from server according to issue #1125
			job.updateInfo();
			if (job.getState() == JobState.Running) {
				job.cancelJob();
			}
			return jobs.remove(job);
		}

		@Override
		public boolean canUpload(Job j, Path p) {
			return uploadFilter.test(j, p);
		}
	};

	private HPCClientProxyAdapter<? extends JobWithDirectorySettings> hpcClient;

	public JobManager(Path workDirectory,
		HPCClientProxyAdapter<? extends JobWithDirectorySettings> hpcClient)
	{
		this.workDirectory = workDirectory;
		this.hpcClient = hpcClient;
	}

	public Job createJob(Object params) throws IOException
	{
		Job result;
		initJobsIfNecessary();
		result = Job.submitNewJob(remover, workDirectory, hpcClient, params);
		jobs.add(result);
		return result;
	}

	public Collection<Job> getJobs() {
		initJobsIfNecessary();
		return Collections.unmodifiableCollection(jobs);
	}

	public void checkConnection() {
		hpcClient.checkConnection();
	}

	public void setUploadFilter(BiPredicate<Job, Path> filter) {
		uploadFilter = filter != null ? filter : DUMMY_UPLOAD_FILTER;
	}

	@Override
	public synchronized void close() {
		if (jobs != null) {
			jobs.forEach(Job::close);
		}
	}

	private synchronized void initJobsIfNecessary() {
		if (jobs == null) {
			jobs = new LinkedList<>();
			try (Stream<Path> dir = Files.list(this.workDirectory)) {
				dir.filter(p -> p.toFile().isDirectory() && Job.isValidJobPath(p))
					.forEach(p -> jobs.add(Job.getExistingJob(remover, p, hpcClient)));
			}
			catch (IOException e) {
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
