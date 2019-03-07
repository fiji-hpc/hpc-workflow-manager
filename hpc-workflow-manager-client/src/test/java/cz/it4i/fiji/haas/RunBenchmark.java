package cz.it4i.fiji.haas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.DummyProgress;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkSPIMParameters;

public class RunBenchmark {
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.RunBenchmark.class);

	public static class CreateJob {
		public static void main(String[] args) throws IOException {
			try(BenchmarkJobManager benchmarkJobManager = new BenchmarkJobManager(getBenchmarkSPIMParameters())) {
				BenchmarkJob ji = benchmarkJobManager.createJob(jd -> jd, jd -> jd);
				log.info("job: " + ji.getId() + " created.");
			}
		}
	}

	public static class ProcessJobs {
		public static void main(String[] args) throws IOException {
			try( BenchmarkJobManager benchmarkJobManager = new BenchmarkJobManager(getBenchmarkSPIMParameters())) {
				for (BenchmarkJob job : benchmarkJobManager.getJobs()) {
					JobState state;
					log.info("job: " + job.getId() + " hasStatus " + (state = job.getState()));
					if (state == JobState.Configuring) {
						job.startJob(new DummyProgress());
					} else if (state != JobState.Running && state != JobState.Queued) {
						job.startDownload();
					}
					else if (state == JobState.Running) {
						log.info(job.getComputationOutput(
							SynchronizableFileType.StandardErrorFile));
					}
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
