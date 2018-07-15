package cz.it4i.fiji.haas_spim_benchmark.core;

import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.BENCHMARK_TASK_NAME_MAP;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.FUSION_SWITCH;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.HAAS_UPDATE_TIMEOUT;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.HDF5_XML_FILENAME;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.SPIM_OUTPUT_FILENAME_PATTERN;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO;

import java.io.BufferedReader;
import java.io.Closeable;
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
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.imagej.updater.util.Progress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.it4i.fiji.haas.HaaSOutputHolder;
import cz.it4i.fiji.haas.HaaSOutputHolderImpl;
import cz.it4i.fiji.haas.Job;
import cz.it4i.fiji.haas.JobManager;
import cz.it4i.fiji.haas_java_client.HaaSClientSettings;
import cz.it4i.fiji.haas_java_client.JobSettings;
import cz.it4i.fiji.haas_java_client.JobSettingsBuilder;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.haas_java_client.UploadingFile;

public class BenchmarkJobManager implements Closeable {

	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.class);

	private final JobManager jobManager;

	public final class BenchmarkJob implements HaaSOutputHolder {

		private final Job job;

		private final List<Task> tasks;
		private final List<BenchmarkError> nonTaskSpecificErrors;
		private final SPIMComputationAccessor computationAccessor;
		private int processedOutputLength;
		private JobState verifiedState;
		private boolean verifiedStateProcessed;
		private CompletableFuture<JobState> running;
		private ProgressNotifier downloadNotifier;

		public BenchmarkJob(Job job) {
			this.job = job;
			tasks = new LinkedList<>();
			nonTaskSpecificErrors = new LinkedList<>();
			computationAccessor = getComputationAccessor();
		}

		public void setDownloadNotifier(Progress progress) {
			job.setDownloadNotifier(downloadNotifier =
				createDownloadNotifierProcessingResultCSV(convertTo(progress)));
		}

		public void setUploadNotifier(Progress progress) {
			job.setUploadNotifier(convertTo(progress));
		}

		public synchronized void startJob(Progress progress) throws IOException {
			job.uploadFile(Constants.CONFIG_YAML, new ProgressNotifierAdapter(progress));
			LoadedYAML yaml = new LoadedYAML(job.openLocalFile(Constants.CONFIG_YAML));

			verifiedState = null;
			verifiedStateProcessed = false;
			running = null;
			job.submit();
			job.setProperty(SPIM_OUTPUT_FILENAME_PATTERN,
					yaml.getCommonProperty(FUSION_SWITCH) + "_" + yaml.getCommonProperty(HDF5_XML_FILENAME));
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

		public CompletableFuture<?> startDownload() throws IOException {
			if (job.getState() == JobState.Finished) {
				CompletableFuture<?> result = new CompletableFuture<Void>();
				startDownloadResults(result);
				return result;
			} else if (job.getState() == JobState.Failed || job.getState() == JobState.Canceled) {
				return job.startDownload(downloadFailedData());
			} else {
				return CompletableFuture.completedFuture(null);
			}
		}

		public boolean canBeDownloaded() {
			return job.canBeDownloaded();
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

		public boolean isUseDemoData() {
			return job.isUseDemoData();
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

		public boolean delete() {
			return job.delete();
		}

		public void cancelJob() {
			job.cancelJob();
		}

		@Override
		public List<String> getActualOutput(List<SynchronizableFileType> content) {
			return computationAccessor.getActualOutput(content);
		}

		public void resumeTransfer() {
			job.resumeDownload();
			job.resumeUpload();
		}

		public void setDownloaded(Boolean val) {
			job.setDownloaded(val);
		}

		public void setUploaded(boolean b) {
			job.setUploaded(b);
		}

		public boolean isDownloaded() {
			return job.isDownloaded();
		}

		public boolean isUploaded() {
			return job.isUploaded();
		}

		public void stopDownload() {
			job.stopDownloadData();
		}

		public boolean needsDownload() {
			return job.needsDownload();
		}

		public boolean needsUpload() {
			return job.needsUpload();
		}

		@Override
		public String toString() {
			return "" + getId();
		}

		public void storeDataInWorkdirectory(UploadingFile file) throws IOException {
			job.storeDataInWorkdirectory(file);
		}

		public Path getInputDirectory() {
			return job.getInputDirectory();
		}

		public Path getOutputDirectory() {
			return job.getOutputDirectory();
		}
		
		public Path getResultXML() {
			return Paths.get(job.getProperty(SPIM_OUTPUT_FILENAME_PATTERN) + ".xml");
		}

		private ProgressNotifier convertTo(Progress progress) {
			return progress == null ? null : new ProgressNotifierAdapter(progress);
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
				@SuppressWarnings("resource")
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
			@SuppressWarnings("resource")
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

		private void startDownloadResults(CompletableFuture<?> result) throws IOException {
			String mainFile = job.getProperty(SPIM_OUTPUT_FILENAME_PATTERN) + ".xml";
			final ProgressNotifierTemporarySwitchOff notifierSwitch = new ProgressNotifierTemporarySwitchOff(
					downloadNotifier, job);

			job.startDownload(downloadFileNameExtractDecorator(fileName -> fileName.equals(mainFile)))
					.whenComplete((X, E) -> {
						notifierSwitch.switchOn();
					}).thenCompose(X -> {
						Set<String> otherFiles = extractNames(getOutputDirectory().resolve(mainFile));
						try {
							return job
									.startDownload(downloadFileNameExtractDecorator( downloadCSVDecorator( name -> otherFiles.contains(name))));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}

					}).whenComplete((X, e) -> {
						if (e != null) {
							log.error(e.getMessage(), e);
						}
						result.complete(null);
					});
		}

		private Set<String> extractNames(Path resolve) {
			Set<String> result = new HashSet<>();
			try (InputStream fileIS = Files.newInputStream(resolve)) {
				DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = builderFactory.newDocumentBuilder();
				Document xmlDocument = builder.parse(fileIS);
				XPath xPath = XPathFactory.newInstance().newXPath();
				Node imageLoader = ((NodeList) xPath.evaluate("/SpimData/SequenceDescription/ImageLoader", xmlDocument,
						XPathConstants.NODESET)).item(0);
				Node hdf5 = ((NodeList) xPath.evaluate("hdf5", imageLoader, XPathConstants.NODESET)).item(0);
				result.add(hdf5.getTextContent());
				NodeList nl = (NodeList) xPath.evaluate("partition/path", imageLoader, XPathConstants.NODESET);
				for (int i = 0; i < nl.getLength(); i++) {
					result.add(nl.item(i).getTextContent());
				}
			} catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
				log.error(e.getMessage(), e);
			}
			return result;
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

				private final HaaSOutputHolder outputOfSnakemake = new HaaSOutputHolderImpl(
						list -> job.getOutput(list));

				@Override
				public List<String> getActualOutput(List<SynchronizableFileType> content) {
					return outputOfSnakemake.getActualOutput(content);
				}

				@Override
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
				}
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

		private ProgressNotifier createDownloadNotifierProcessingResultCSV(
			ProgressNotifier progressNotifier)
		{
			if (progressNotifier == null) return null;
			return new DownloadNotifierProcessingResultCSV(progressNotifier, this);
		}
	}

	public BenchmarkJobManager(BenchmarkSPIMParameters params) {
		jobManager = new JobManager(params.workingDirectory(), constructSettingsFromParams(params));
		jobManager.setUploadFilter(this::canUpload);
	}

	public BenchmarkJob createJob(Function<Path, Path> inputDirectoryProvider,
			Function<Path, Path> outputDirectoryProvider) throws IOException {
		Job job = jobManager.createJob( getJobSettings(),inputDirectoryProvider, outputDirectoryProvider);
		if(job.getInputDirectory() == null) {
			job.createEmptyFile(Constants.DEMO_DATA_SIGNAL_FILE_NAME);
		}
		return convertJob(job);
	}

	public Collection<BenchmarkJob> getJobs() {
		return jobManager.getJobs().stream().map(this::convertJob).collect(Collectors.toList());
	}

	public static void formatResultFile(Path filename) {

		List<ResultFileTask> identifiedTasks = new LinkedList<>();

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
						jobs.add(new ResultFileJob());
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
				if (fileWriter != null) {
					fileWriter.flush();
					fileWriter.close();
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void close() {
		jobManager.close();
	}

	private boolean canUpload(Job job, Path p) {
		return job.getInputDirectory() == null || !p.equals(job.getInputDirectory().resolve(Constants.CONFIG_YAML));
	}

	private BenchmarkJob convertJob(Job job) {
		return new BenchmarkJob(job);
	}

	private static JobSettings getJobSettings() {
		return new JobSettingsBuilder().jobName(Constants.HAAS_JOB_NAME)
				.clusterNodeType(Constants.HAAS_CLUSTER_NODE_TYPE).templateId(Constants.HAAS_TEMPLATE_ID)
				.walltimeLimit(Constants.HAAS_TIMEOUT).numberOfCoresPerNode(Constants.CORES_PER_NODE).build();
	}

	static private Predicate<String> downloadFileNameExtractDecorator(Predicate<String> decorated) {
		return name -> {
			Path path = getPathSafely(name);
			if (path == null)
				return false;

			String fileName = path.getFileName().toString();
			return decorated.test(fileName);
		};
	}
	
	static private Predicate<String> downloadCSVDecorator(Predicate<String> decorated) {
		return name -> {
			if(name.toLowerCase().endsWith(".csv")) {
				return true;
			}
			return decorated.test(name);
		};
		
	}

	static private Predicate<String> downloadFailedData() {
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

	private static HaaSClientSettings constructSettingsFromParams(BenchmarkSPIMParameters params) {
		return new HaaSClientSettings() {

			@Override
			public String getUserName() {
				return params.username();
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
			public String getEmail() {
				return params.email();
			}

		};
	}

}
