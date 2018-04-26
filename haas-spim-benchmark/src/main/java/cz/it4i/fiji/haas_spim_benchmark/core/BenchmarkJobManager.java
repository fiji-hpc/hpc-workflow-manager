package cz.it4i.fiji.haas_spim_benchmark.core;

import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.BENCHMARK_RESULT_FILE;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.BENCHMARK_TASK_NAME_MAP;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.HAAS_UPDATE_TIMEOUT;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.SPIM_OUTPUT_FILENAME_PATTERN;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import cz.it4i.fiji.haas.HaaSOutputHolder;
import cz.it4i.fiji.haas.HaaSOutputHolderImpl;
import cz.it4i.fiji.haas.Job;
import cz.it4i.fiji.haas.JobManager;
import cz.it4i.fiji.haas.UploadingFileFromResource;
import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import cz.it4i.fiji.haas_java_client.Settings;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import net.imagej.updater.util.Progress;

public class BenchmarkJobManager {

	private static final String JOB_HAS_DATA_TO_DOWNLOAD_PROPERTY = "job.needDownload";

	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.class);

	private JobManager jobManager;

	public final class BenchmarkJob implements HaaSOutputHolder {

		private final Job job;
		private final List<Task> tasks;
		private final List<BenchmarkError> nonTaskSpecificErrors;
		private final SPIMComputationAccessor computationAccessor;
		private int processedOutputLength;
		private JobState verifiedState;
		private boolean verifiedStateProcessed;
		private CompletableFuture<JobState> running;

		public BenchmarkJob(Job job) {
			this.job = job;
			tasks = new LinkedList<Task>();
			nonTaskSpecificErrors = new LinkedList<BenchmarkError>();
			computationAccessor = getComputationAccessor();
		}

		public synchronized void startJob(Progress progress) throws IOException {
			job.uploadFile(Constants.CONFIG_YAML,  new P_ProgressNotifierAdapter(progress));
			String outputName = getOutputName(job.openLocalFile(Constants.CONFIG_YAML));
			verifiedState = null;
			verifiedStateProcessed = false;
			running = null;
			job.submit();
			job.setProperty(SPIM_OUTPUT_FILENAME_PATTERN, outputName);
			setDownloaded(false);
		}

		public JobState getState() {
			return getStateAsync(r -> r.run()).getNow(JobState.Unknown);
		}

		public void startUpload() {
			job.startUploadData();
		}

		public void stopUpload() {
			job.stopUploadData();
		}

		public boolean isUploading() {
			return job.isUploading();
		}

		public synchronized CompletableFuture<JobState> getStateAsync(Executor executor) {
			if (running != null) {
				return running;
			}
			CompletableFuture<JobState> result = doGetStateAsync(executor);
			if (!result.isCancelled() && !result.isCompletedExceptionally() && !result.isDone()) {
				running = result;
			}
			return result;
		}

		public void downloadData(Progress progress) throws IOException {
			if (job.getState() == JobState.Finished) {
				String filePattern = job.getProperty(SPIM_OUTPUT_FILENAME_PATTERN);
				job.download(downloadFinishedData(filePattern), new P_ProgressNotifierAdapter(progress));
			} else if (job.getState() == JobState.Failed || job.getState() == JobState.Canceled) {
				job.download(downloadFailedData(), new P_ProgressNotifierAdapter(progress));
			}

			setDownloaded(true);
		}

		public void downloadStatistics(Progress progress) throws IOException {
			job.download(BenchmarkJobManager.downloadStatistics(), new P_ProgressNotifierAdapter(progress));
			Path resultFile = job.getDirectory().resolve(BENCHMARK_RESULT_FILE);
			if (resultFile != null)
				BenchmarkJobManager.formatResultFile(resultFile);
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

		@Override
		public int hashCode() {
			return Long.hashCode(job.getId());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof BenchmarkJob) {
				return ((BenchmarkJob) obj).getId() == getId();
			}
			return false;
		}

		public boolean downloaded() {
			return getDownloaded();
		}

		public void update() {
			job.updateInfo();
		}

		public Path getDirectory() {
			return job.getDirectory();
		}

		public List<Task> getTasks() {

			// If no tasks have been identified, try to search through the output
			if (tasks.isEmpty()) {
				fillTasks();
			}

			// Should you (finally) have some, try to parse the output further, otherwise
			// just give up
			if (!tasks.isEmpty()) {
				processOutput();
			}

			return tasks;
		}

		public void exploreErrors() {
			for (BenchmarkError error : getErrors()) {
				System.out.println(error.getPlainDescription());
			}
		}

		public String getAnotherOutput() {
			return computationAccessor.getActualOutput(Arrays.asList(SynchronizableFileType.StandardOutputFile)).get(0);
		}

		public String getSnakemakeOutput() {
			return computationAccessor.getActualOutput(Arrays.asList(SynchronizableFileType.StandardErrorFile)).get(0);
		}

		public boolean remove() {
			return job.remove();
		}

		public void cancelJob() {
			job.cancelJob();
		}

		@Override
		public List<String> getActualOutput(List<SynchronizableFileType> content) {
			return computationAccessor.getActualOutput(content);
		}

		@Override
		public String toString() {
			return "" + getId();
		}

		private synchronized CompletableFuture<JobState> doGetStateAsync(Executor executor) {
			JobState state = job.getState();
			if (state != JobState.Finished) {
				return CompletableFuture.completedFuture(state);
			} else if (verifiedState != null) {
				return CompletableFuture.completedFuture(verifiedState);
			}
			verifiedStateProcessed = true;
			return CompletableFuture.supplyAsync(() -> {
				try {
					verifiedState = Stream.concat(Arrays.asList(state).stream(),
							getTasks().stream().filter(task -> !task.getDescription().equals(Constants.DONE_TASK))
									.flatMap(task -> task.getComputations().stream()).map(tc -> tc.getState()))
							.max(new JobStateComparator()).get();
					if (verifiedState != JobState.Finished && verifiedState != JobState.Canceled) {
						verifiedState = JobState.Failed;
					}
					synchronized (BenchmarkJob.this) {
						// test whether job was restarted - it sets running to null
						if (!verifiedStateProcessed) {
							verifiedState = null;
							return doGetStateAsync(r -> r.run()).getNow(null);
						}
						running = null;
						return verifiedState;
					}
				} finally {
					synchronized (BenchmarkJob.this) {
						if (running != null) {
							running = null;
						}
					}
				}
			}, executor);
		}

		private String getStringFromTimeSafely(Calendar time) {
			return time != null ? time.getTime().toString() : "N/A";
		}

		private void fillTasks() {

			final String OUTPUT_PARSING_JOB_COUNTS = "Job counts:";
			final String OUTPUT_PARSING_TAB_DELIMITER = "\\t";
			final int OUTPUT_PARSING_EXPECTED_NUMBER_OF_WORDS_PER_LINE = 2;
			final String OUTPUT_PARSING_WORKFLOW_ERROR = "WorkflowError";
			final String OUTPUT_PARSING_VALUE_ERROR = "ValueError";

			processedOutputLength = -1;
			int readJobCountIndex = -1;
			boolean found = false;
			String output = getSnakemakeOutput();

			// Found last job count definition
			while (true) {
				readJobCountIndex = output.indexOf(OUTPUT_PARSING_JOB_COUNTS, processedOutputLength + 1);

				if (readJobCountIndex < 0) {
					break;
				}

				found = true;
				processedOutputLength = readJobCountIndex;
			}

			// If no job count definition has been found, search through the output and list
			// all errors
			if (!found) {
				Scanner scanner = new Scanner(getSnakemakeOutput());
				String currentLine;
				while (scanner.hasNextLine()) {
					currentLine = scanner.nextLine().trim();
					if (currentLine.contains(OUTPUT_PARSING_WORKFLOW_ERROR) //
							|| currentLine.contains(OUTPUT_PARSING_VALUE_ERROR)) {
						String errorMessage = "";
						while (!currentLine.isEmpty()) {
							errorMessage += currentLine;
							if (!scanner.hasNextLine()) {
								break;
							}
							currentLine = scanner.nextLine().trim();
						}
						nonTaskSpecificErrors.add(new BenchmarkError(errorMessage));
					}
				}
				scanner.close();
				return;
			}

			// After the job count definition, task specification is expected
			Scanner scanner = new Scanner(output.substring(processedOutputLength));
			scanner.nextLine(); // Immediately after job count definition, task specification table header is
								// expected
			while (scanner.hasNextLine()) {
				if (scanner.nextLine().trim().isEmpty()) {
					continue;
				}

				while (true) {
					List<String> lineWords = Arrays.stream(scanner.nextLine().split(OUTPUT_PARSING_TAB_DELIMITER))
							.filter(word -> word.length() > 0).collect(Collectors.toList());
					if (lineWords.size() != OUTPUT_PARSING_EXPECTED_NUMBER_OF_WORDS_PER_LINE) {
						break;
					}
					tasks.add(new Task(computationAccessor, lineWords.get(1), Integer.parseInt(lineWords.get(0))));
				}
				break;
			}
			scanner.close();

			// Order tasks chronologically
			if (!tasks.isEmpty()) {
				List<String> chronologicList = BENCHMARK_TASK_NAME_MAP.keySet().stream().collect(Collectors.toList());
				Collections.sort(tasks,
						Comparator.comparingInt(task -> chronologicList.indexOf(task.getDescription())));
			}
		}

		private void processOutput() {

			final String OUTPUT_PARSING_RULE = "rule ";
			final String OUTPUT_PARSING_COLON = ":";

			String output = getSnakemakeOutput().substring(processedOutputLength);
			int outputLengthToBeProcessed = output.length();

			int ruleRelativeIndex = -1;
			int colonRelativeIndex = -1;
			while (true) {

				ruleRelativeIndex = output.indexOf(OUTPUT_PARSING_RULE, colonRelativeIndex);
				colonRelativeIndex = output.indexOf(OUTPUT_PARSING_COLON, ruleRelativeIndex);

				if (ruleRelativeIndex == -1 || colonRelativeIndex == -1) {
					break;
				}

				String taskDescription = output.substring(ruleRelativeIndex + OUTPUT_PARSING_RULE.length(),
						colonRelativeIndex);
				List<Task> task = tasks.stream().filter(t -> t.getDescription().equals(taskDescription))
						.collect(Collectors.toList());
				if (1 == task.size()) {
					// TODO: Consider throwing an exception
					task.get(0).populateTaskComputationParameters(processedOutputLength + ruleRelativeIndex);
				}
			}

			processedOutputLength = processedOutputLength + outputLengthToBeProcessed;
		}

		private SPIMComputationAccessor getComputationAccessor() {
			SPIMComputationAccessor result = new SPIMComputationAccessor() {

				private HaaSOutputHolder outputOfSnakemake = new HaaSOutputHolderImpl(list -> job.getOutput(list));

				@Override
				public List<String> getActualOutput(List<SynchronizableFileType> content) {
					return outputOfSnakemake.getActualOutput(content);
				}

				public java.util.Collection<String> getChangedFiles() {
					return job.getChangedFiles();
				}

				@Override
				public List<Long> getFileSizes(List<String> names) {
					return job.getFileSizes(names);
				}

				@Override
				public List<String> getFileContents(List<String> logs) {
					return job.getFileContents(logs);
				};
			};

			result = new SPIMComputationAccessorDecoratorWithTimeout(result,
					new HashSet<>(Arrays.asList(SynchronizableFileType.StandardOutputFile,
							SynchronizableFileType.StandardErrorFile)),
					HAAS_UPDATE_TIMEOUT / UI_TO_HAAS_FREQUENCY_UPDATE_RATIO);
			return result;
		}

		private List<BenchmarkError> getErrors() {
			getTasks();
			Stream<BenchmarkError> taskSpecificErrors = tasks.stream().flatMap(s -> s.getErrors().stream());
			return Stream.concat(nonTaskSpecificErrors.stream(), taskSpecificErrors).collect(Collectors.toList());
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

	public static void formatResultFile(Path filename) throws FileNotFoundException {

		List<ResultFileTask> identifiedTasks = new LinkedList<ResultFileTask>();

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

				String[] columns = line.split(Constants.DELIMITER);

				if (columns[0].equals(Constants.STATISTICS_TASK_NAME)) {

					// If there is a task being processed, add all cached jobs to it and wrap it up
					if (null != processedTask) {
						processedTask.setJobs(jobs);
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
			if (null != processedTask) {
				processedTask.setJobs(jobs);
				identifiedTasks.add(processedTask);
			}

		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return;
		}

		// Order tasks chronologically
		List<String> chronologicList = BENCHMARK_TASK_NAME_MAP.keySet().stream().collect(Collectors.toList());
		Collections.sort(identifiedTasks, Comparator.comparingInt(t -> chronologicList.indexOf(t.getName())));

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(
					filename.getParent().toString() + Constants.FORWARD_SLASH + Constants.STATISTICS_SUMMARY_FILENAME);
			fileWriter.append(Constants.SUMMARY_FILE_HEADER).append(Constants.NEW_LINE_SEPARATOR);

			for (ResultFileTask task : identifiedTasks) {
				fileWriter.append(Constants.BENCHMARK_TASK_NAME_MAP.get(task.getName())).append(Constants.DELIMITER);
				fileWriter.append(Double.toString(task.getAverageMemoryUsage())).append(Constants.DELIMITER);
				fileWriter.append(Double.toString(task.getAverageWallTime())).append(Constants.DELIMITER);
				fileWriter.append(Double.toString(task.getMaximumWallTime())).append(Constants.DELIMITER);
				fileWriter.append(Double.toString(task.getTotalTime())).append(Constants.DELIMITER);
				fileWriter.append(Integer.toString(task.getJobCount()));
				fileWriter.append(Constants.NEW_LINE_SEPARATOR);
			}

			Double pipelineStart = identifiedTasks.stream() //
					.mapToDouble(t -> t.getEarliestStartInSeconds()).min().getAsDouble();

			Double pipelineEnd = identifiedTasks.stream() //
					.mapToDouble(t -> t.getLatestEndInSeconds()).max().getAsDouble();

			fileWriter.append(Constants.NEW_LINE_SEPARATOR);
			fileWriter.append("Pipeline duration: " + (pipelineEnd - pipelineStart));

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
					|| path.getParent() != null && path.getParent().getFileName() != null
							&& path.getParent().getFileName().toString().equals("logs");
		};
	}

	private static Path getPathSafely(String name) {
		try {
			return Paths.get(name);
		} catch (InvalidPathException ex) {
			return null;
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

	private class P_ProgressNotifierAdapter implements ProgressNotifier {
		private Progress progress;

		public P_ProgressNotifierAdapter(Progress progress) {
			this.progress = progress;
		}

		public void setTitle(String title) {
			progress.setTitle(title);
		}

		public void setCount(int count, int total) {
			progress.setCount(count, total);
		}

		public void addItem(Object item) {
			progress.addItem(item);
		}

		public void setItemCount(int count, int total) {
			progress.setItemCount(count, total);
		}

		public void itemDone(Object item) {
			progress.itemDone(item);
		}

		public void done() {
			progress.done();
		}

	}

}
