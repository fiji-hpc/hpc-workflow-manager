package cz.it4i.fiji.haas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.JobManager.JobSynchronizableFile;
import cz.it4i.fiji.haas.ui.DummyProgress;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.Job;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkSPIMParameters;

public class RunBenchmark {
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.RunBenchmark.class);

	public static class CreateJob {
		public static void main(String[] args) throws IOException {
			BenchmarkJobManager benchmarkJobManager = new BenchmarkJobManager(getBenchmarkSPIMParameters());
			Job ji = benchmarkJobManager.createJob();
			log.info("job: " + ji.getId() + " created.");
		}
	}

	public static class ProcessJobs {
		public static void main(String[] args) throws IOException {
			BenchmarkJobManager benchmarkJobManager = new BenchmarkJobManager(getBenchmarkSPIMParameters());
			for (Job job : benchmarkJobManager.getJobs()) {
				JobState state;
				log.info("job: " + job.getId() + " hasStatus " + (state = job.getState()));
				if (state == JobState.Configuring) {
					job.startJob(new DummyProgress());
				} else if (state != JobState.Running && state != JobState.Queued) {
					job.downloadData(new DummyProgress());
				} else if (state == JobState.Running) {
					JobSynchronizableFile file = new JobSynchronizableFile(SynchronizableFileType.StandardErrorFile, 0);
					log.info(job.getOutput(Arrays.asList(file)).iterator().next());
				}
			}
		}
	}

	

	private static BenchmarkSPIMParameters getBenchmarkSPIMParameters() throws IOException {
		Path p = Paths.get("/tmp/benchmark");
		if (!Files.exists(p)) {
			Files.createDirectory(p);
		}
		return new TestBenchmarkSPIMParametersImpl(p);
	}
}
