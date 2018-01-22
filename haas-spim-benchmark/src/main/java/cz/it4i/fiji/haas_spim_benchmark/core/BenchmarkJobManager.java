package cz.it4i.fiji.haas_spim_benchmark.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import cz.it4i.fiji.haas.JobManager;
import cz.it4i.fiji.haas.JobManager.JobInfo;
import cz.it4i.fiji.haas.JobManager.JobSynchronizableFile;
import cz.it4i.fiji.haas.UploadingFileFromResource;
import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.Settings;
import javafx.beans.value.ObservableValueBase;
import net.imagej.updater.util.Progress;

public class BenchmarkJobManager {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.class);

	public final class Job extends ObservableValueBase<Job> {
		private JobInfo jobInfo;

		private JobState oldState;

		public Job(JobInfo ji) {
			super();
			this.jobInfo = ji;
		}

		public void startJob(Progress progress) throws IOException {
			jobInfo.uploadFilesByName(Arrays.asList(CONFIG_YAML), progress);
			String outputName = getOutputName(jobInfo.openLocalFile(CONFIG_YAML));
			jobInfo.submit();
			jobInfo.setProperty(SPIM_OUTPUT_FILENAME_PATTERN, outputName);
		}

		public JobState getState() {
			return oldState = jobInfo.getState();
		}

		public void downloadData(Progress progress) throws IOException {
			JobInfo ji = jobInfo;
			if (ji.needsDownload()) {
				if (ji.getState() == JobState.Finished) {
					String filePattern = ji.getProperty(SPIM_OUTPUT_FILENAME_PATTERN);
					ji.downloadData(downloadFinishedData(filePattern), progress, false);
				} else if (ji.getState() == JobState.Failed) {
					ji.downloadData(downloadFailedData(), progress, false);
				}
			}
		}

		public void downloadStatistics(Progress progress) throws IOException {
			JobInfo ji = jobInfo;
			ji.downloadData(BenchmarkJobManager.downloadStatistics(), progress, true);
		}

		public List<String> getOutput(List<JobSynchronizableFile> files) {
			return jobInfo.getOutput(files);
		}

		public long getId() {
			return jobInfo.getId();
		}

		public String getCreationTime() {
			return jobInfo.getCreationTime();
		}

		public String getStartTime() {
			return jobInfo.getStartTime();
		}

		public String getEndTime() {
			return jobInfo.getEndTime();
		}

		@Override
		public Job getValue() {
			return this;
		}

		@Override
		public int hashCode() {
			return jobInfo.getId().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Job) {
				Job job = (Job) obj;
				return job.getId() == getId();

			}
			return false;
		}

		public void update(Job job) {
			jobInfo = job.jobInfo;
			if (jobInfo.getState() != oldState) {
				fireValueChangedEvent();
			}
		}

		public boolean downloaded() {
			return !jobInfo.needsDownload();
		}

		public Job update() {
			jobInfo.updateInfo();
			return this;
		}

		public Path getDirectory() {
			return jobInfo.getDirectory();
		}
	}

	private static final String HAAS_JOB_NAME = "HaaSSPIMBenchmark";
	private static final int HAAS_CLUSTER_NODE_TYPE = 6;
	private static final int HAAS_TEMPLATE_ID = 4;
	private static final String HAAS_PROJECT_ID = "DD-17-31";
	private static final int HAAS_TIMEOUT = 9600;

	private static final String SPIM_OUTPUT_FILENAME_PATTERN = "spim.outputFilenamePattern";
	private static final String CONFIG_YAML = "config.yaml";

	private JobManager jobManager;

	public BenchmarkJobManager(BenchmarkSPIMParameters params) throws IOException {
		jobManager = new JobManager(params.workingDirectory(), constructSettingsFromParams(params));
	}

	public Job createJob() throws IOException {
		JobInfo jobInfo = jobManager.createJob();
		jobInfo.storeDataInWorkdirectory(getUploadingFile());
		return convertJob(jobInfo);
	}

	public Collection<Job> getJobs() throws IOException {

		return jobManager.getJobs().stream().map(this::convertJob).collect(Collectors.toList());
	}

	private HaaSClient.UploadingFile getUploadingFile() {
		return new UploadingFileFromResource("", CONFIG_YAML);
	}

	private Job convertJob(JobInfo jobInfo) {
		return new Job(jobInfo);
	}

	private String getOutputName(InputStream openLocalFile) throws IOException {
		try (InputStream is = openLocalFile) {
			Yaml yaml = new Yaml();

			Map<String, Map<String, String>> map = yaml.load(is);
			String result = map.get("common").get("hdf5_xml_filename");
			if (result == null) {
				throw new IllegalArgumentException("hdf5_xml_filename not found");
			}
			if (result.charAt(0) == '"' || result.charAt(0) == '\'') {
				if (result.charAt(result.length() - 1) != result.charAt(0)) {
					throw new IllegalArgumentException(result);
				}
				result = result.substring(1, result.length() - 1);
			}

			return result;
		}

	}

	private static Predicate<String> downloadFinishedData(String filePattern) {
		return name -> {
			Path path = getPathSafely(name);
			if (path == null)
				return false;
			
			String fileName = path.getFileName().toString();
			return fileName.startsWith(filePattern) && fileName.endsWith("h5") || fileName.equals(filePattern + ".xml")
					|| fileName.equals("benchmark_result.csv");
		};
	}

	private static Predicate<String> downloadStatistics() {
		return name -> {
			Path path = getPathSafely(name);
			if (path == null)
				return false;
			
			String fileName = path.getFileName().toString();
			return fileName.equals("benchmark_result.csv");
		};
	}

	private static Predicate<String> downloadFailedData() {
		return name -> {
			Path path = getPathSafely(name);
			if (path == null)
				return false;
			
			return path.getFileName().toString().startsWith("snakejob.")
					|| path.getParent().getFileName().toString().equals("logs");
		};
	}
	
	private static Path getPathSafely(String name) {
		try {
			return Paths.get(name);
		} catch(InvalidPathException ex) {
			return null;
		}
	}

	private static Settings constructSettingsFromParams(BenchmarkSPIMParameters params) {
		// TODO Auto-generated method stub
		return new Settings() {

			@Override
			public String getUserName() {
				return params.username();
			}

			@Override
			public int getTimeout() {
				return HAAS_TIMEOUT;
			}

			@Override
			public long getTemplateId() {
				return HAAS_TEMPLATE_ID;
			}

			@Override
			public String getProjectId() {
				return HAAS_PROJECT_ID;
			}

			@Override
			public String getPhone() {
				return params.phone();
			}

			@Override
			public String getPassword() {
				return params.password();
			}

			@Override
			public String getJobName() {
				return HAAS_JOB_NAME;
			}

			@Override
			public String getEmail() {
				return params.email();
			}

			@Override
			public long getClusterNodeType() {
				return HAAS_CLUSTER_NODE_TYPE;
			}
		};
	}
}
