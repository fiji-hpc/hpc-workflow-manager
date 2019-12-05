
package cz.it4i.fiji.hpc_workflow.core;

import static cz.it4i.fiji.hpc_client.JobState.Canceled;
import static cz.it4i.fiji.hpc_client.JobState.Failed;
import static cz.it4i.fiji.hpc_client.JobState.Finished;
import static cz.it4i.fiji.hpc_workflow.core.Constants.BENCHMARK_TASK_NAME_MAP;
import static cz.it4i.fiji.hpc_workflow.core.Constants.DONE_TASK;
import static cz.it4i.fiji.hpc_workflow.core.Constants.FUSION_SWITCH;
import static cz.it4i.fiji.hpc_workflow.core.Constants.HDF5_XML_FILENAME;
import static cz.it4i.fiji.hpc_workflow.core.Constants.SPIM_OUTPUT_FILENAME_PATTERN;
import static cz.it4i.fiji.hpc_workflow.core.Constants.VERIFIED_STATE_OF_FINISHED_JOB;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.Status;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.it4i.common.WebRoutines;
import cz.it4i.fiji.hpc_adapter.HPCClientProxyAdapter;
import cz.it4i.fiji.hpc_adapter.Job;
import cz.it4i.fiji.hpc_adapter.JobManager;
import cz.it4i.fiji.hpc_adapter.UploadingFileFromResource;
import cz.it4i.fiji.hpc_client.HPCClient;
import cz.it4i.fiji.hpc_client.HPCClientException;
import cz.it4i.fiji.hpc_client.JobState;
import cz.it4i.fiji.hpc_client.ProgressNotifier;
import cz.it4i.fiji.hpc_client.SynchronizableFileType;
import cz.it4i.fiji.hpc_client.UploadingFile;
import cz.it4i.fiji.hpc_client.data_transfer.FileTransferInfo;
import cz.it4i.fiji.hpc_client.data_transfer.Synchronization;
import cz.it4i.fiji.hpc_workflow.Task;
import cz.it4i.fiji.hpc_workflow.TaskComputation;
import cz.it4i.fiji.hpc_workflow.WorkflowJob;
import cz.it4i.fiji.hpc_workflow.WorkflowParadigm;

@Plugin(type = ParallelizationParadigm.class)
public class HPCWorkflowJobManager<T extends JobWithWorkflowTypeSettings>
	implements WorkflowParadigm<T>
{

	public interface DownloadingStatusProvider {

		boolean isDownloaded();

		boolean needsDownload();
	}

	private static final String JOB_HAAS_TEMPLATE_ID = "job.haas_template_id";

	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_workflow.core.HPCWorkflowJobManager.class);

	private JobManager jobManager;

	private BooleanSupplier initializator;

	private Runnable finalizer;

	private Runnable initDoneCallback;

	private HPCClientProxyAdapter<T> hpcClient;

	private Path workingDirectory;

	public static final class BenchmarkJob implements MacroWorkflowJob {

		private final Job job;
		private final SnakemakeOutputHelper snakemakeOutputHelper;
		private JobState verifiedState;
		private boolean verifiedStateProcessed;
		private CompletableFuture<JobState> running;
		private ProgressNotifier downloadNotifier;
		private DownloadingStatusProvider downloadingStatus =
			new DownloadingStatusProvider()
			{

				@Override
				public boolean needsDownload() {
					return job.needsDownload();
				}

				@Override
				public boolean isDownloaded() {
					return job.isDownloaded();
				}
			};

		private boolean visibleInBDV;

		public BenchmarkJob(Job job) {
			this.job = job;
			snakemakeOutputHelper = new SnakemakeOutputHelper(job);
		}

		@Override
		public synchronized void startJob(ProgressNotifier progress)
			throws IOException
		{
			LoadedYAML yaml = null;
			if (getWorkflowType() == WorkflowType.SPIM_WORKFLOW) {
				job.uploadFile(Constants.CONFIG_YAML, progress);
				yaml = new LoadedYAML(job.openLocalFile(Constants.CONFIG_YAML));
			}

			verifiedStateProcessed = false;
			running = null;
			String message = "Submitting job id #" + getId();
			progress.addItem(message);

			job.updateInfo();
			JobState oldState = updateAndGetState();
			job.submit();
			setVerifiedState(null);
			while (oldState == updateAndGetState()) {
				try {
					wait(Constants.WAIT_FOR_SUBMISSION_TIMEOUT);
				}
				catch (InterruptedException exc) {
					log.error(exc.getMessage(), exc);
					Thread.currentThread().interrupt();
				}
			}
			progress.itemDone(message);
			if (getWorkflowType() == WorkflowType.SPIM_WORKFLOW && yaml != null) {
				job.setProperty(SPIM_OUTPUT_FILENAME_PATTERN, yaml.getCommonProperty(
					FUSION_SWITCH) + "_" + yaml.getCommonProperty(HDF5_XML_FILENAME));
			} else {
				job.setLastStartedTimestamp();
			}

		}

		@Override
		public boolean delete() {
			return job.delete();
		}

		@Override
		public void cancelJob() {
			job.cancelJob();
		}

		@Override
		public JobState getState() {
			return getStateAsync(Runnable::run).getNow(JobState.Unknown);
		}

		@Override
		public synchronized CompletableFuture<JobState> getStateAsync(
			Executor executor)
		{
			if (running != null) {
				return running;
			}
			CompletableFuture<JobState> result = doGetStateAsync(executor);
			if (!result.isCancelled() && !result.isCompletedExceptionally() && !result
				.isDone())
			{
				running = result;
			}
			return result;
		}

		@Override
		public void update() {
			job.updateInfo();
			try {
				visibleInBDV = getPathToLocalResultFile().toFile().exists() ||
					WebRoutines.doesURLExist(new URL(getPathToRemoteResultFile()));
				if (log.isDebugEnabled()) {
					log.debug("job # ".concat(getId().toString()).concat(
						" is visible in BDV ").concat(Boolean.toString(visibleInBDV)));
				}
			}
			catch (MalformedURLException exc) {
				log.info(exc.getMessage(), exc);
				visibleInBDV = false;
			}
		}

		@Override
		public Path getPathToLocalResultFile() {
			return getOutputDirectory().resolve(getResultXML());
		}

		@Override
		public String getPathToRemoteResultFile() {
			String changed = job.getId() + "/" + getResultXML();
			MessageDigest digest;
			try {
				digest = MessageDigest.getInstance("SHA-1");
				digest.reset();
				digest.update(changed.getBytes("utf8"));
				String sha1 = String.format("%040x", new BigInteger(1, digest
					.digest()));
				String result = Configuration.getBDSAddress() + sha1 + "/";
				if (log.isDebugEnabled()) {
					log.debug("getBDSPathForData changed={} path={}", result, result);
				}
				return result;
			}
			catch (NoSuchAlgorithmException | UnsupportedEncodingException exc) {
				throw new RuntimeException(exc);
			}
		}

		@Override
		public boolean isVisibleInBDV() {
			return visibleInBDV;
		}

		@Override
		public void startUpload() {
			job.startUploadData();
		}

		@Override
		public void stopUpload() {
			job.stopUploadData();
		}

		@Override
		public boolean isUploading() {
			return job.isUploading();
		}

		@Override
		public void setUploadNotifier(ProgressNotifier progress) {
			job.setUploadNotifier(progress);
		}

		@Override
		public void setUploaded(boolean b) {
			job.setUploaded(b);
		}

		@Override
		public boolean isUploaded() {
			return job.isUploaded();
		}

		@Override
		public CompletableFuture<?> startDownload() throws IOException {
			if (job.getState() == Finished) {
				CompletableFuture<?> result = new CompletableFuture<>();
				startDownloadResults(result);
				return result;
			}
			else if (job.getState() == Failed || job.getState() == Canceled) {
				return job.startDownload(downloadFailedData());
			}
			else {
				return CompletableFuture.completedFuture(null);
			}
		}

		@Override
		public void stopDownload() {
			job.stopDownloadData();
		}

		@Override
		public void setDownloadNotifier(ProgressNotifier progress) {
			downloadNotifier = createDownloadNotifierProcessingResultCSV(progress);
			job.setDownloadNotifier(downloadNotifier);
		}

		@Override
		public boolean canBeDownloaded() {
			return job.canBeDownloaded();
		}

		@Override
		public void setDownloaded(boolean val) {
			job.setDownloaded(val);
		}

		@Override
		public boolean isDownloaded() {
			return downloadingStatus.isDownloaded();
		}

		@Override
		public boolean isDownloading() {
			return job.isDownloading();
		}

		@Override
		public void resumeTransfer() {
			job.resumeDownload();
			job.resumeUpload();
		}

		@Override
		public boolean canBeUploaded() {
			return !job.isUseDemoData();
		}

		@Override
		public String getCreationTime() {
			return getStringFromTimeSafely(job.getCreationTime());
		}

		@Override
		public String getStartTime() {
			return getStringFromTimeSafely(job.getStartTime());
		}

		@Override
		public String getEndTime() {
			return getStringFromTimeSafely(job.getEndTime());
		}

		@Override
		public Path getDirectory() {
			return job.getDirectory();
		}

		@Override
		public List<Task> getTasks() {
			return snakemakeOutputHelper.getTasks();
		}

		@Override
		public void exploreErrors() {
			for (HPCWorkflowError error : snakemakeOutputHelper.getErrors()) {
				log.error(error.getPlainDescription());
			}
		}

		@Override
		public List<String> getComputationOutput(
			final List<SynchronizableFileType> types)
		{
			return snakemakeOutputHelper.getActualOutput(types);
		}

		public void storeDataInWorkdirectory(UploadingFile file)
			throws IOException
		{
			job.storeDataInWorkdirectory(file);
		}

		@Override
		public Path getInputDirectory() {
			return job.getInputDirectory();
		}

		@Override
		public Path getOutputDirectory() {
			return job.getOutputDirectory();
		}

		public String getResultXML() {
			return job.getProperty(SPIM_OUTPUT_FILENAME_PATTERN) + ".xml";
		}

		@Override
		public List<FileTransferInfo> getFileTransferInfo() {
			return job.getFileTransferInfo();
		}

		@Override
		public Long getId() {
			return job.getId();
		}
		
		@Override
		public List<String> getFileContents(List<String> files) {
			return job.getFileContents(files);
		}

		@Override
		public String getUserScriptName() {
			return job.getUserScriptName();
		}
		
		@Override
		public void setLastStartedTimestamp() {
			job.setLastStartedTimestamp();
		}

		public void setWorkflowType(WorkflowType workflowType) {
			job.setProperty(JOB_HAAS_TEMPLATE_ID, workflowType.name());
		}

		@Override
		public WorkflowType getWorkflowType() {
			String strValue = job.getProperty(JOB_HAAS_TEMPLATE_ID);
			try {
				return WorkflowType.valueOf(strValue);
			}
			catch (java.lang.IllegalArgumentException exc) {
				if (exc.getMessage().startsWith("No enum constant " + WorkflowType.class
					.getCanonicalName() + "."))
				{
					try {
						return WorkflowType.forLong(Long.parseLong(strValue));
					}
					catch (NumberFormatException exc2) {
						throw exc;
					}
				}

				throw exc;

			}
		}

		@Override
		public String getWorkflowTypeName() {
			return getWorkflowType().name();
		}

		@Override
		public Comparator<? extends WorkflowJob> getComparator() {
			return (BenchmarkJob j1, BenchmarkJob j2) -> (int) (j1.job.getId() -
				j2.job.getId());
		}

		@Override
		public String toString() {
			return "" + getId();
		}

		@Override
		public int hashCode() {
			return Long.hashCode(job.getId());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof BenchmarkJob) {
				return Objects.equals(((BenchmarkJob) obj).getId(), getId());
			}
			return false;
		}

		private synchronized CompletableFuture<JobState> doGetStateAsync(
			Executor executor)
		{
			JobState state = job.getState();
			if (state != Finished) {
				setVerifiedState(null);
				return CompletableFuture.completedFuture(state);
			}
			if (getVerifiedState() != null) {
				return CompletableFuture.completedFuture(getVerifiedState());
			}

			verifiedStateProcessed = true;
			return CompletableFuture.supplyAsync(() -> {
				try {
					JobState workVerifiedState = Stream.concat(Arrays.asList(state)
						.stream(), getTasks().stream().filter(task -> !task.getDescription()
							.equals(DONE_TASK)).flatMap(task -> task.getComputations()
								.stream()).map(TaskComputation::getState)).max(
									new JobStateComparator()).get();

					if (workVerifiedState != Finished && workVerifiedState != Canceled) {
						workVerifiedState = Failed;
					}
					synchronized (BenchmarkJob.this) {
						// test whether job was restarted - it sets running to null
						if (!verifiedStateProcessed) {
							workVerifiedState = null;
							return doGetStateAsync(Runnable::run).getNow(null);
						}
						running = null;
						setVerifiedState(workVerifiedState);
						return workVerifiedState;
					}
				}
				finally {
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

		private void startDownloadResults(CompletableFuture<?> result)
			throws IOException
		{

			final WorkflowType jobType = getWorkflowType();
			final String mainFile = job.getProperty(SPIM_OUTPUT_FILENAME_PATTERN) +
				".xml";
			final StillRunningDownloadSwitcher stillRunningTemporarySwitch =
				new StillRunningDownloadSwitcher(() -> downloadingStatus,
					val -> downloadingStatus = val);
			final ProgressNotifierTemporarySwitchOff progressNotifierTemporarySwitchOff =
				new ProgressNotifierTemporarySwitchOff(downloadNotifier, job);
			downloadMainFile(jobType, mainFile, stillRunningTemporarySwitch,
				progressNotifierTemporarySwitchOff).thenCompose(x -> {
					try {
						return job.startDownload(downloadFileNameExtractDecorator(
							downloadCSVDecorator(getOtherFilesFilterForDownload(jobType,
								mainFile))));
					}
					catch (IOException e) {
						throw new RuntimeException(e);
					}
					finally {
						progressNotifierTemporarySwitchOff.switchOn();
						stillRunningTemporarySwitch.switchBack();
					}
				}).whenComplete((x, e) -> {
					if (e != null) {
						log.error(e.getMessage(), e);
						downloadNotifier.addItem(Synchronization.FAILED_ITEM);
					}
					result.complete(null);
				});
		}

		private Predicate<String> getOtherFilesFilterForDownload(
			WorkflowType jobType, String mainFile)
		{
			if (jobType == WorkflowType.MACRO_WORKFLOW) {
				return x -> true;
			}
			Set<String> otherFiles = extractNames(getOutputDirectory().resolve(
				mainFile));
			return otherFiles::contains;
		}

		private CompletableFuture<?> downloadMainFile(final WorkflowType jobType,
			final String mainFile,
			final StillRunningDownloadSwitcher stillRunningTemporarySwitch,
			final ProgressNotifierTemporarySwitchOff progressNotifierTemporarySwitchOff)
			throws IOException
		{
			if (jobType == WorkflowType.MACRO_WORKFLOW) {
				return CompletableFuture.completedFuture(null);
			}
			return job.startDownload(downloadFileNameExtractDecorator(
				fileName -> fileName.equals(mainFile))).exceptionally(x -> {
					progressNotifierTemporarySwitchOff.switchOn();
					stillRunningTemporarySwitch.switchBack();
					return null;
				});
		}

		private Set<String> extractNames(Path pathToXML) {
			Set<String> result = new HashSet<>();
			try (InputStream fileIS = Files.newInputStream(pathToXML)) {
				DocumentBuilderFactory builderFactory = DocumentBuilderFactory
					.newInstance();
				DocumentBuilder builder = builderFactory.newDocumentBuilder();
				Document xmlDocument = builder.parse(fileIS);
				XPath xPath = XPathFactory.newInstance().newXPath();
				Node imageLoader = ((NodeList) xPath.evaluate(
					"/SpimData/SequenceDescription/ImageLoader", xmlDocument,
					XPathConstants.NODESET)).item(0);
				Node hdf5 = ((NodeList) xPath.evaluate("hdf5", imageLoader,
					XPathConstants.NODESET)).item(0);
				result.add(hdf5.getTextContent());
				NodeList nl = (NodeList) xPath.evaluate("partition/path", imageLoader,
					XPathConstants.NODESET);
				for (int i = 0; i < nl.getLength(); i++) {
					result.add(nl.item(i).getTextContent());
				}
			}
			catch (IOException | ParserConfigurationException | SAXException
					| XPathExpressionException e)
			{
				throw new HPCClientException("Extract names from " + pathToXML, e);
			}
			return result;
		}

		private ProgressNotifier createDownloadNotifierProcessingResultCSV(
			ProgressNotifier progressNotifier)
		{
			if (progressNotifier == null) return null;
			return new DownloadNotifierProcessingResultCSV(progressNotifier, this);
		}

		private void setVerifiedState(JobState value) {
			verifiedState = value;
			job.setProperty(VERIFIED_STATE_OF_FINISHED_JOB, value != null ? value
				.toString() : null);
		}

		private JobState getVerifiedState() {
			if (verifiedState == null) {
				String storedVerifiedState = job.getProperty(
					VERIFIED_STATE_OF_FINISHED_JOB);
				if (storedVerifiedState != null) {
					verifiedState = JobState.valueOf(storedVerifiedState);
				}
			}
			return verifiedState;
		}

		private JobState updateAndGetState() {
			job.updateInfo();
			return job.getState();
		}

		private Predicate<String> downloadFailedData() {
			return name -> {
				Path path = getPathSafely(name);
				if (path == null) return false;
				return path.getFileName().toString().startsWith("snakejob.") || path
					.getParent() != null && path.getParent().getFileName() != null && path
						.getParent().getFileName().toString().equals("logs");
			};
		}

		private Predicate<String> downloadFileNameExtractDecorator(
			Predicate<String> decorated)
		{
			return name -> {
				Path path = getPathSafely(name);
				if (path == null) return false;

				String fileName = path.getFileName().toString();
				return decorated.test(fileName);
			};
		}

		private Path getPathSafely(String name) {
			try {
				return Paths.get(name);
			}
			catch (InvalidPathException ex) {
				return null;
			}
		}

		private Predicate<String> downloadCSVDecorator(
			Predicate<String> decorated)
		{
			return name -> {
				if (name.toLowerCase().endsWith(".csv")) {
					return true;
				}
				return decorated.test(name);
			};

		}

		@Override
		public long getLastStartedTimestamp() {
			return job.getLastStartedTimestamp();
		}
	}

	public void prepareParadigm(
		Path aWorkingDirectory,
		Supplier<HPCClient<T>> hpcClientSupplier,
		Class<T> jobSettingsType, BooleanSupplier aInitializator,
		Runnable aInitDoneCallback, Runnable aFinalizer)
	{
		this.workingDirectory = aWorkingDirectory;
		this.hpcClient = new HPCClientProxyAdapter<>(hpcClientSupplier,
			jobSettingsType);
		this.initializator = aInitializator;
		this.finalizer = aFinalizer;
		this.initDoneCallback = aInitDoneCallback;
	}


	@Override
	public WorkflowJob createJob(T parameters) throws IOException {

		Job job = jobManager.createJob(parameters);
		if (job.getInputDirectory() == null) {
			job.createEmptyFile(Constants.DEMO_DATA_SIGNAL_FILE_NAME);
		}
		BenchmarkJob result = convertJob(job);
		result.setWorkflowType(parameters.getWorkflowType());
		if (job.isUseDemoData()) {
			job.storeDataInWorkdirectory(getConfigYamlFile());
		}
		return result;
	}

	@Override
	public Collection<WorkflowJob> getJobs() {
		return jobManager.getJobs().stream().map(this::convertJob).collect(
			Collectors.toList());
	}

	@Override
	public Class<T> getTypeOfJobSettings() {
		return hpcClient.getTypeOfJobSettings();
	}

	@Override
	public void checkConnection() {
		jobManager.checkConnection();
	}

	public static void formatResultFile(Path filename) {

		List<ResultFileTask> identifiedTasks = new LinkedList<>();

		try (BufferedReader reader = Files.newBufferedReader(filename)) {
			String line = null;

			ResultFileTask processedTask = null;
			List<ResultFileJob> jobs = new LinkedList<>();

			while (null != (line = reader.readLine())) {

				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}

				String[] columns = line.split(Constants.DELIMITER);

				if (columns[0].equals(Constants.STATISTICS_TASK_NAME)) {

					// If there is a task being processed, add all cached jobs to it and
					// wrap it up
					if (null != processedTask) {
						processedTask.setJobs(jobs);
						identifiedTasks.add(processedTask);
					}

					// Start processing a new task
					processedTask = new ResultFileTask(columns[1]);
					jobs.clear();

				}
				else if (columns[0].equals(Constants.STATISTICS_JOB_IDS)) {

					// Cache all found jobs
					for (int i = 1; i < columns.length; i++) {
						jobs.add(new ResultFileJob());
					}

				}
				else if (!columns[0].equals(Constants.STATISTICS_JOB_COUNT)) {

					// Save values of a given property to cached jobs
					for (int i = 1; i < columns.length; i++) {
						jobs.get(i - 1).setValue(columns[0], columns[i]);
					}

				}
			}

			// If there is a task being processed, add all cached jobs to it and wrap
			// it up
			if (null != processedTask) {
				processedTask.setJobs(jobs);
				identifiedTasks.add(processedTask);
			}

		}
		catch (IOException e) {
			log.error(e.getMessage(), e);
			return;
		}

		// Order tasks chronologically
		List<String> chronologicList = BENCHMARK_TASK_NAME_MAP.keySet().stream()
			.collect(Collectors.toList());
		Collections.sort(identifiedTasks, Comparator.comparingInt(
			t -> chronologicList.indexOf(t.getName())));

		try (FileWriter fileWriter = new FileWriter(filename.getParent()
			.toString() + Constants.FORWARD_SLASH +
			Constants.STATISTICS_SUMMARY_FILENAME))
		{
			fileWriter.append(Constants.SUMMARY_FILE_HEADER).append(
				Constants.NEW_LINE_SEPARATOR);

			for (ResultFileTask task : identifiedTasks) {
				fileWriter.append(Constants.BENCHMARK_TASK_NAME_MAP.get(task.getName()))
					.append(Constants.DELIMITER);
				fileWriter.append(Double.toString(task.getAverageMemoryUsage())).append(
					Constants.DELIMITER);
				fileWriter.append(Double.toString(task.getAverageWallTime())).append(
					Constants.DELIMITER);
				fileWriter.append(Double.toString(task.getMaximumWallTime())).append(
					Constants.DELIMITER);
				fileWriter.append(Double.toString(task.getTotalTime())).append(
					Constants.DELIMITER);
				fileWriter.append(Integer.toString(task.getJobCount()));
				fileWriter.append(Constants.NEW_LINE_SEPARATOR);
			}

			Double pipelineStart = identifiedTasks.stream() //
				.mapToDouble(ResultFileTask::getEarliestStartInSeconds).min()
				.getAsDouble();

			Double pipelineEnd = identifiedTasks.stream() //
				.mapToDouble(ResultFileTask::getLatestEndInSeconds).max().getAsDouble();

			fileWriter.append(Constants.NEW_LINE_SEPARATOR);
			fileWriter.append("Pipeline duration: " + (pipelineEnd - pipelineStart));

		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public final synchronized void init() {
		if (jobManager != null) {
			throw new IllegalStateException("This paradigm is already initialized.");
		}
		if (initializator != null && !initializator.getAsBoolean()) {
			return;
		}
		jobManager = new JobManager(workingDirectory, hpcClient);
		jobManager.setUploadFilter(this::canUpload);
		checkConnection();
		if (initDoneCallback != null) {
			initDoneCallback.run();
		}
	}

	@Override
	public synchronized Status getStatus() {
		return jobManager == null ? Status.NON_ACTIVE : Status.ACTIVE;
	}

	@Override
	public synchronized void close() {
		if (jobManager == null) {
			throw new IllegalStateException(
				"This paradigm is not already initialized.");
		}
		jobManager.close();
		jobManager = null;
		if (finalizer != null) {
			finalizer.run();
		}
	}

	private boolean canUpload(Job job, Path p) {
		return job.getInputDirectory() == null || !p.equals(job.getInputDirectory()
			.resolve(Constants.CONFIG_YAML));
	}

	private BenchmarkJob convertJob(Job job) {
		return new BenchmarkJob(job);
	}

	private static UploadingFile getConfigYamlFile() {
		return new UploadingFileFromResource("", Constants.CONFIG_YAML);
	}


}
