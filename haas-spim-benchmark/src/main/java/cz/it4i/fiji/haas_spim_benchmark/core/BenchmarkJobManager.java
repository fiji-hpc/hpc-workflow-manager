package cz.it4i.fiji.haas_spim_benchmark.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
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

	private static final String JOB_HAS_DATA_TO_DOWNLOAD_PROPERTY = "job.needDownload";
	
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
			jobInfo.uploadFilesByName(Arrays.asList(Constants.CONFIG_YAML), progress);
			String outputName = getOutputName(jobInfo.openLocalFile(Constants.CONFIG_YAML));
			jobInfo.submit();
			jobInfo.setProperty(Constants.SPIM_OUTPUT_FILENAME_PATTERN, outputName);
			setDownloaded(false);
		}

		public JobState getState() {
			return oldState = jobInfo.getState();
		}

		public void downloadData(Progress progress) throws IOException {
			JobInfo ji = jobInfo;
			if (ji.getState() == JobState.Finished) {
				String filePattern = ji.getProperty(Constants.SPIM_OUTPUT_FILENAME_PATTERN);
				ji.downloadData(downloadFinishedData(filePattern), progress);
			} else if (ji.getState() == JobState.Failed) {
				ji.downloadData(downloadFailedData(), progress);
			}
			setDownloaded(true);
		}

		public void downloadStatistics(Progress progress) throws IOException {
			JobInfo ji = jobInfo;
			ji.downloadData(BenchmarkJobManager.downloadStatistics(), progress);
			Path resultFile = ji.getDirectory().resolve(Constants.BENCHMARK_RESULT_FILE);
			if (resultFile != null)
				BenchmarkJobManager.formatResultFile(resultFile);
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
			return getDownloaded();
		}

		public Job update() {
			jobInfo.updateInfo();
			return this;
		}

		public Path getDirectory() {
			return jobInfo.getDirectory();
		}
		

		private void setDownloaded(boolean b) {
			jobInfo.setProperty(JOB_HAS_DATA_TO_DOWNLOAD_PROPERTY, b + "");
		}
		
		private boolean getDownloaded() {
			String downloadedStr = jobInfo.getProperty(JOB_HAS_DATA_TO_DOWNLOAD_PROPERTY);
			return downloadedStr != null && Boolean.parseBoolean(downloadedStr);
		}
	}

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
		return new UploadingFileFromResource("", Constants.CONFIG_YAML);
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
					|| fileName.equals(Constants.BENCHMARK_RESULT_FILE);
		};
	}

	private static Predicate<String> downloadStatistics() {
		return name -> {
			Path path = getPathSafely(name);
			if (path == null)
				return false;
			
			String fileName = path.getFileName().toString();
			return fileName.equals(Constants.BENCHMARK_RESULT_FILE);
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
	
	private static void formatResultFile(Path filename) throws FileNotFoundException {
		
		List<ResultFileTask> identifiedTasks = new LinkedList<ResultFileTask>();
		
		try {
			String line = null;
			final String separator = ";";
			
			ResultFileTask processedTask = null;			
			List<ResultFileJob> jobs = new LinkedList<>();
			
			BufferedReader reader = Files.newBufferedReader(filename);
			while (null != (line = reader.readLine())) {
				
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				
				String[] columns = line.split(separator);
				
				if (columns[0].equals(Constants.STATISTICS_TASK_NAME)) {
					
					// If there is a task being processed, add all cached jobs to it and wrap it up
					if (null != processedTask ) {
						processedTask.jobs.addAll(jobs);
						identifiedTasks.add(processedTask);
					}
					
					// Start processing a new task
					processedTask = new ResultFileTask(columns[1]);
					jobs.clear();
					
				} else if (columns[0].equals(Constants.STATISTICS_JOB_IDS)) {
					
					// Cache all found jobs
					for (int i = 1; i < columns.length; i++) {
						jobs.add(new ResultFileJob(columns[i]));
					}
					
				} else if (!columns[0].equals(Constants.STATISTICS_JOB_COUNT)) {
					
					// Save values of a given property to cached jobs
					for (int i = 1; i < columns.length; i++) {
						jobs.get(i - 1).setValue(columns[0], columns[i]);
					}
					
				} 
			}
			
			// If there is a task being processed, add all cached jobs to it and wrap it up
			if (null != processedTask ) {
				processedTask.jobs.addAll(jobs);
				identifiedTasks.add(processedTask);
			}
			
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} 
		
		for (ResultFileTask task : identifiedTasks) {
			Object[] args = {Constants.STATISTICS_TASK_NAME_MAP.get(task.name), task.getJobCount(), task.getAverageMemoryUsage()};
			MessageFormat fmt = new MessageFormat(Constants.STATISTICS_OUTPUT_MESSAGE);
			System.out.println(fmt.format(args));
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
				return Constants.HAAS_TIMEOUT;
			}

			@Override
			public long getTemplateId() {
				return Constants.HAAS_TEMPLATE_ID;
			}

			@Override
			public String getProjectId() {
				return Constants.HAAS_PROJECT_ID;
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
				return Constants.HAAS_JOB_NAME;
			}

			@Override
			public String getEmail() {
				return params.email();
			}

			@Override
			public long getClusterNodeType() {
				return Constants.HAAS_CLUSTER_NODE_TYPE;
			}
		};
	}
}
