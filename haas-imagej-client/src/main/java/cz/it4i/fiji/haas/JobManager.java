package cz.it4i.fiji.haas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.scijava.Context;

import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.JobState;
import javafx.beans.value.ObservableValueBase;

public class JobManager {

	
	
	private Path workDirectory;
	
	private Collection<Job> jobs = new LinkedList<>();
	
	private HaaSClient haasClient;

	private Context context;

	

	public JobManager(Path workDirectory, Context ctx) throws IOException {
		super();
		this.context = ctx;
		this.workDirectory = workDirectory;
		context.inject(this);
		Files.list(this.workDirectory).filter(p -> Files.isDirectory(p) && Job.isJobPath(p))
				.forEach(p -> {
					try {
						jobs.add(inject(new Job(p,this::getHaasClient)));
					} catch (IOException e) {
						e.printStackTrace();
					}
				});

	}

	private Job inject(Job job) {
		context.inject(job);
		return job;
	}

	public void startJob(Path path, Collection<Path> files) throws IOException {
		jobs.add(new Job(path, files,this::getHaasClient));
	}
	
	public Iterable<JobInfo> getJobsNeedingDownload() {
		return ()->jobs.stream().filter(j->j.needsDownload()).map(j->new JobInfo(j)).iterator();
	}

	public Iterable<JobInfo> getJobs() {
		return ()->jobs.stream().map(j->new JobInfo(j)).iterator();
	}
	
	public void downloadJob(Long id) {
		Iterator<Job>  job =jobs.stream().filter(j->j.getJobId() == id).iterator();
		assert job.hasNext();
		job.next().download();
		
	}

	private HaaSClient getHaasClient() {
		if(haasClient == null) {
			haasClient = new HaaSClient(2l, 9600, 6l, "DD-17-31");
		}
		return haasClient;
	}

	public static class JobInfo extends ObservableValueBase<JobInfo> {

		private Job job;
		
		
		public JobInfo(Job job) {
			this.job = job;
		}

		public Long getId() {
			return job.getJobId();
		}

		public JobState getState() {
			return job.getState();
		}

		public boolean needsDownload() {
			return job.needsDownload();
		}
		
		public String getStartTime() {
			return job.getStartTime().getTime().toString();
		}
		
		public String getEndTime() {
			return job.getEndTime().getTime().toString();
		}

		public void downloadData() {
			job.download();
			fireValueChangedEvent();
		}
		
		@Override
		public JobInfo getValue() {
			return this;
		}
	}
}
