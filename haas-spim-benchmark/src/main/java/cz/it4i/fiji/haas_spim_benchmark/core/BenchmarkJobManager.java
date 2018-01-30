package cz.it4i.fiji.haas_spim_benchmark.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import cz.it4i.fiji.haas.HaaSOutputHolder;
import cz.it4i.fiji.haas.HaaSOutputHolderImpl;
import cz.it4i.fiji.haas.HaaSOutputSource;
import cz.it4i.fiji.haas.Job;
import cz.it4i.fiji.haas.JobManager;
import cz.it4i.fiji.haas.JobManager.JobSynchronizableFile;
import cz.it4i.fiji.haas.UploadingFileFromResource;
import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.Settings;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import net.imagej.updater.util.Progress;

public class BenchmarkJobManager {

	private static final String JOB_HAS_DATA_TO_DOWNLOAD_PROPERTY = "job.needDownload";
	
	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.class);

	private JobManager jobManager;
	

	public final class BenchmarkJob implements HaaSOutputSource {
		
		private Job job;
		
		private HaaSOutputHolder outputOfSnakemake;

		private List<Task> tasks;

		private SPIMComputationAccessor computationAccessor = new SPIMComputationAccessor() {
			@Override
			public String getActualOutput() {
				return outputOfSnakemake.getActualOutput();
			}
			
			@Override
			public boolean fileExists(String fileName) {
				// TASK 1011 modify interface of job for checking of file existence
				return false;
			}
		};
		
		
		public BenchmarkJob(Job job) {
			super();
			this.job = job;
			outputOfSnakemake = new HaaSOutputHolderImpl(this, SynchronizableFileType.StandardErrorFile);
		}

		public void startJob(Progress progress) throws IOException {
			job.uploadFilesByName(Arrays.asList(Constants.CONFIG_YAML), progress);
			String outputName = getOutputName(job.openLocalFile(Constants.CONFIG_YAML));
			job.submit();
			job.setProperty(Constants.SPIM_OUTPUT_FILENAME_PATTERN, outputName);
			setDownloaded(false);
		}

		public JobState getState() {
			return job.getState();
		}

		public void downloadData(Progress progress) throws IOException {
			if (job.getState() == JobState.Finished) {
				String filePattern = job.getProperty(Constants.SPIM_OUTPUT_FILENAME_PATTERN);
				job.download(downloadFinishedData(filePattern), progress);
			} else if (job.getState() == JobState.Failed) {
				job.download(downloadFailedData(), progress);
			}
			
			setDownloaded(true);
		}

		public void downloadStatistics(Progress progress) throws IOException {			
			job.download(BenchmarkJobManager.downloadStatistics(), progress);
			Path resultFile = job.getDirectory().resolve(Constants.BENCHMARK_RESULT_FILE);
			if (resultFile != null)
				BenchmarkJobManager.formatResultFile(resultFile);
		}

		public List<String> getOutput(List<JobSynchronizableFile> files) {
			return job.getOutput(files);
		}
		
		public long getId() {
			return job.getId();
		}

		public String getCreationTime() {
			return getStringFromTimeSafely(job.getCreationTime());
		}

		public String getStartTime() {
			return getStringFromTimeSafely(job.getStartTime());
		}

		public String getEndTime() {
			return getStringFromTimeSafely(job.getEndTime());
		}
		
		private String getStringFromTimeSafely(Calendar time) {
			return time != null ? time.getTime().toString() : "N/A";
		}

		@Override
		public int hashCode() {
			return Long.hashCode(job.getId());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof BenchmarkJob) {
				BenchmarkJob job = (BenchmarkJob) obj;
				return job.getId() == getId();
			}
			return false;
		}

		
		public boolean downloaded() {
			return getDownloaded();
		}

		public BenchmarkJob update() {
			job.updateInfo();
			return this;
		}
	
		public Path getDirectory() {
			return job.getDirectory();
		}
		
		public List<Task> getTasks() {
			if(tasks == null) {
				fillTasks();
			}
			return tasks;
		}
		

		private void fillTasks() {
			SPIMComputationAccessor accessor = computationAccessor;
			String snakeMakeoutput = outputOfSnakemake.getActualOutput();
			//TASK 1011 parse snakeOutput, create tasks base part:
//Job counts:
//			count	jobs
//			1	define_output
//			1	define_xml_tif
//			1	done
//			2	fusion
//			1	hdf5_xml
//			1	hdf5_xml_output
//			2	registration
//			2	resave_hdf5
//			2	resave_hdf5_output
//			1	timelapse
//			1	xml_merge
//			15
			
		}
		private void setDownloaded(boolean b) {
			job.setProperty(JOB_HAS_DATA_TO_DOWNLOAD_PROPERTY, b + "");
		}
		
		private boolean getDownloaded() {
			String downloadedStr = job.getProperty(JOB_HAS_DATA_TO_DOWNLOAD_PROPERTY);
			return downloadedStr != null && Boolean.parseBoolean(downloadedStr);
		}
	}

	public BenchmarkJobManager(BenchmarkSPIMParameters params) throws IOException {
		jobManager = new JobManager(params.workingDirectory(), constructSettingsFromParams(params));
	}

	public BenchmarkJob createJob() throws IOException {
		Job job = jobManager.createJob();
		job.storeDataInWorkdirectory(getUploadingFile());
		return convertJob(job);
	}

	public Collection<BenchmarkJob> getJobs() throws IOException {
		return jobManager.getJobs().stream().map(this::convertJob).collect(Collectors.toList());
	}


	private HaaSClient.UploadingFile getUploadingFile() {
		return new UploadingFileFromResource("", Constants.CONFIG_YAML);
	}

	private BenchmarkJob convertJob(Job job) {
		return new BenchmarkJob(job);
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
		
		final String newLineSeparator = "\n";
		final String delimiter = ";";
		final String summaryFileHeader = "Task;MemoryUsage;WallTime;JobCount";
		
		try {
			String line = null;
			
			ResultFileTask processedTask = null;			
			List<ResultFileJob> jobs = new LinkedList<>();
			
			BufferedReader reader = Files.newBufferedReader(filename);
			while (null != (line = reader.readLine())) {
				
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				
				String[] columns = line.split(delimiter);
				
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
		
		FileWriter fileWriter = null;		
		try {			
			fileWriter = new FileWriter(filename.getParent().toString() + "/" + Constants.STATISTICS_SUMMARY_FILENAME);
			fileWriter.append(summaryFileHeader).append(newLineSeparator);
			
			for (ResultFileTask task : identifiedTasks) {
				fileWriter.append(Constants.STATISTICS_TASK_NAME_MAP.get(task.name)).append(delimiter);
				fileWriter.append(Double.toString(task.getAverageMemoryUsage())).append(delimiter);
				fileWriter.append(Double.toString(task.getAverageWallTime())).append(delimiter);
				fileWriter.append(Integer.toString(task.getJobCount()));
				fileWriter.append(newLineSeparator);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private static Settings constructSettingsFromParams(BenchmarkSPIMParameters params) {
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
