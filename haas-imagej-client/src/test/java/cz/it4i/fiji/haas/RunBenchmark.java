package cz.it4i.fiji.haas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.JobManager.JobInfo;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import net.imagej.updater.util.Progress;

public class RunBenchmark {
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.RunBenchmark.class);

	public static class CreateJob {
		public static void main(String[] args) throws IOException {
			Path p = Paths.get("/tmp/benchmark");
			if (!Files.exists(p)) {
				Files.createDirectory(p);
			}
			BenchmarkJobManager benchmarkJobManager = new BenchmarkJobManager(p, new P_Progress());
			JobInfo ji = benchmarkJobManager.createJob();
			log.info("job: " + ji + " created.");
		}
	}

	public static class ProcessJobs {
		public static void main(String[] args) throws IOException {
			Path p = Paths.get("/tmp/benchmark");
			if (!Files.exists(p)) {
				Files.createDirectory(p);
			}
			BenchmarkJobManager benchmarkJobManager = new BenchmarkJobManager(p, new P_Progress());
			for (JobInfo ji : benchmarkJobManager.getJobs()) {
				log.info("job: " + ji.getId() + " hasStatus " + ji.getState());
				if (ji.getState() == JobState.Configuring) {
					benchmarkJobManager.startJob(ji);
				} else if (ji.getState() != JobState.Running && ji.getState() != JobState.Queued) {
					ji.downloadData(new P_Progress());
				} else if (ji.getState() == JobState.Running) {
					log.info(ji.getOutput(Arrays.asList(
							new JobManager.JobSynchronizableFile(SynchronizableFileType.StandardErrorFile, 0))).iterator().next());
				}
			}
		}
	}

	private static class P_Progress implements Progress {

		@Override
		public void setTitle(String title) {
		}

		@Override
		public void setCount(int count, int total) {
		}

		@Override
		public void addItem(Object item) {
		}

		@Override
		public void setItemCount(int count, int total) {
		}

		@Override
		public void itemDone(Object item) {
		}

		@Override
		public void done() {
		}

	}
}
