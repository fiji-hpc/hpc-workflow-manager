
package cz.it4i.fiji.haas_spim_benchmark.core;

import static cz.it4i.fiji.haas.data_transfer.PersistentSynchronizationProcess.FAILED_ITEM;
import static cz.it4i.fiji.haas_java_client.JobState.Canceled;
import static cz.it4i.fiji.haas_java_client.JobState.Failed;
import static cz.it4i.fiji.haas_java_client.JobState.Finished;
import static cz.it4i.fiji.haas_spim_benchmark.core.Configuration.getHaasClusterNodeType;
import static cz.it4i.fiji.haas_spim_benchmark.core.Configuration.getWalltime;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.BENCHMARK_TASK_NAME_MAP;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.CORES_PER_NODE;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.DONE_TASK;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.FUSION_SWITCH;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.HAAS_JOB_NAME;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.HDF5_XML_FILENAME;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.SPIM_OUTPUT_FILENAME_PATTERN;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.VERIFIED_STATE_OF_FINISHED_JOB;

import java.io.BufferedReader;
import java.io.Closeable;
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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
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

import cz.it4i.fiji.commons.WebRoutines;
import cz.it4i.fiji.haas.Job;
import cz.it4i.fiji.haas.JobManager;
import cz.it4i.fiji.haas_java_client.FileTransferInfo;
import cz.it4i.fiji.haas_java_client.HaaSClientException;
import cz.it4i.fiji.haas_java_client.HaaSClientSettings;
import cz.it4i.fiji.haas_java_client.JobSettings;
import cz.it4i.fiji.haas_java_client.JobSettingsBuilder;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.haas_java_client.UploadingFile;
import cz.it4i.fiji.haas_spim_benchmark.ui.NewJobController;
import cz.it4i.fiji.haas_spim_benchmark.ui.NewJobController.WorkflowType;

public class BenchmarkJobManager implements Closeable {

	public interface DownloadingStatusProvider {

		boolean isDownloaded();

		boolean needsDownload();
	}

	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.class);

	private final JobManager jobManager;

	public final class BenchmarkJob {

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

		public synchronized void startJob(Progress progress) throws IOException {
			job.uploadFile(Constants.CONFIG_YAML, new ProgressNotifierAdapter(
				progress));
			LoadedYAML yaml = new LoadedYAML(job.openLocalFile(
				Constants.CONFIG_YAML));
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
			job.setProperty(SPIM_OUTPUT_FILENAME_PATTERN, yaml.getCommonProperty(
				FUSION_SWITCH) + "_" + yaml.getCommonProperty(HDF5_XML_FILENAME));
		}

		public boolean delete() {
			return job.delete();
		}

		public void cancelJob() {
			job.cancelJob();
		}

		public JobState getState() {
			return getStateAsync(Runnable::run).getNow(JobState.Unknown);
		}

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

		public void update() {
			job.updateInfo();
			try {
				visibleInBDV = getLocalPathToResultXML().toFile().exists() ||
					WebRoutines.doesURLExist(new URL(getPathToToResultXMLOnBDS()));
				if (log.isDebugEnabled()) {
					log.debug("job # ".concat(Long.toString(getId())).concat(
						" is visible in BDV ").concat(Boolean.toString(visibleInBDV)));
				}
			}
			catch (MalformedURLException exc) {
				log.info(exc.getMessage(), exc);
				visibleInBDV = false;
			}
		}

		public Path getLocalPathToResultXML() {
			return getOutputDirectory().resolve(getResultXML());
		}

		public String getPathToToResultXMLOnBDS() {
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

		public boolean isVisibleInBDV() {
			return visibleInBDV;
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

		public void setUploadNotifier(Progress progress) {
			job.setUploadNotifier(convertToProgressNotifier(progress));
		}

		public void setUploaded(boolean b) {
			job.setUploaded(b);
		}

		public boolean isUploaded() {
			return job.isUploaded();
		}

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

		public void stopDownload() {
			job.stopDownloadData();
		}

		public void setDownloadNotifier(Progress progress) {
			downloadNotifier = createDownloadNotifierProcessingResultCSV(
				convertToProgressNotifier(progress));
			job.setDownloadNotifier(downloadNotifier);
		}

		public boolean canBeDownloaded() {
			return job.canBeDownloaded();
		}

		public void setDownloaded(Boolean val) {
			job.setDownloaded(val);
		}

		public boolean isDownloaded() {
			return downloadingStatus.isDownloaded();
		}

		public boolean isDownloading() {			
			return job.isDownloading();
		}

		public void resumeTransfer() {
			job.resumeDownload();
			job.resumeUpload();
		}

		public boolean isUseDemoData() {
			return job.isUseDemoData();
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

		public Path getDirectory() {
			return job.getDirectory();
		}

		public List<Task> getTasks() {
			return snakemakeOutputHelper.getTasks();
		}

		public void exploreErrors() {
			for (BenchmarkError error : snakemakeOutputHelper.getErrors()) {
				log.error(error.getPlainDescription());
			}
		}

		public String getComputationOutput(final SynchronizableFileType type) {
			return getComputationOutput(Arrays.asList(type)).get(0);
		}

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

		public Path getInputDirectory() {
			return job.getInputDirectory();
		}

		public Path getOutputDirectory() {
			return job.getOutputDirectory();
		}

		public String getResultXML() {
			return job.getProperty(SPIM_OUTPUT_FILENAME_PATTERN) + ".xml";
		}

		public List<FileTransferInfo> getFileTransferInfo() {
			return job.getFileTransferInfo();
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
				return ((BenchmarkJob) obj).getId() == getId();
			}
			return false;
		}

		private ProgressNotifier convertToProgressNotifier(Progress progress) {
			return progress == null ? null : new ProgressNotifierAdapter(progress);
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

		private boolean enableDownload(String fileName, String mainFile) {
			if (WorkflowType.forLong(job
				.getHaasTemplateId()) == WorkflowType.SPIM_WORKFLOW)
			{
				return fileName.equals(mainFile);
			}
			else if (WorkflowType.forLong(job
				.getHaasTemplateId()) == WorkflowType.MACRO_WORKFLOW)
			{
				return true;
			}
			return false;
		}

		private void startDownloadResults(CompletableFuture<?> result)
			throws IOException
		{
			WorkflowType jobType = WorkflowType.forLong(job.getHaasTemplateId());

			final String mainFile = job.getProperty(SPIM_OUTPUT_FILENAME_PATTERN) +
				".xml";

			final StillRunningDownloadSwitcher stillRunningTemporarySwitch =
				new StillRunningDownloadSwitcher(() -> downloadingStatus,
					val -> downloadingStatus = val);
			final ProgressNotifierTemporarySwitchOff progressNotifierTemporarySwitchOff =
				new ProgressNotifierTemporarySwitchOff(downloadNotifier, job);

			job.startDownload(downloadFileNameExtractDecorator(
				fileName -> enableDownload(fileName, mainFile))).exceptionally(temp -> {
					progressNotifierTemporarySwitchOff.switchOn();
					stillRunningTemporarySwitch.switchBack();
					return null;
				}).thenCompose(x -> {
					if (jobType == WorkflowType.SPIM_WORKFLOW) {
						Set<String> otherFiles = extractNames(getOutputDirectory().resolve(
							mainFile));

						try {
							return job.startDownload(downloadFileNameExtractDecorator(
								downloadCSVDecorator(otherFiles::contains)));
						}
						catch (IOException e) {
							throw new RuntimeException(e);
						}
						finally {
							progressNotifierTemporarySwitchOff.switchOn();
							stillRunningTemporarySwitch.switchBack();
						}
					}
					return null;
				}).whenComplete((x, e) -> {
					if (e != null) {
						if (!(e.getCause() instanceof NullPointerException)) {
							log.error(e.getMessage(), e);
							downloadNotifier.addItem(FAILED_ITEM);
						}
						else {
							job.setProperty("job.downloaded", true);
							downloadNotifier.done();
						}
					}
					result.complete(null);
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
				throw new HaaSClientException("Extract names from " + pathToXML, e);
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
		
		public List<String> getFileContents(List<String> files) {
			return job.getFileContents(files);		
		}
		
		public WorkflowType getWorkflowType() {
			return WorkflowType.forLong(job.getHaasTemplateId());
		}

		public String getHaasTemplateName() {
			return NewJobController.WorkflowType.forLong(job.getHaasTemplateId())
				.toString();
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
	}

	public BenchmarkJobManager(BenchmarkSPIMParameters params) {
		jobManager = new JobManager(params.workingDirectory(),
			constructSettingsFromParams(params));
		jobManager.setUploadFilter(this::canUpload);
	}

	public BenchmarkJob createJob(UnaryOperator<Path>  inputDirectoryProvider,
		UnaryOperator<Path> outputDirectoryProvider, int numberOfNodes,
		int haasTemplateId) throws IOException
	{
		Job job = jobManager.createJob(getJobSettings(numberOfNodes,
			haasTemplateId), inputDirectoryProvider, outputDirectoryProvider);
		if (job.getInputDirectory() == null) {
			job.createEmptyFile(Constants.DEMO_DATA_SIGNAL_FILE_NAME);
		}
		return convertJob(job);
	}

	public Collection<BenchmarkJob> getJobs() {
		return jobManager.getJobs().stream().map(this::convertJob).collect(
			Collectors.toList());
	}

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
	public void close() {
		jobManager.close();
	}

	private boolean canUpload(Job job, Path p) {
		return job.getInputDirectory() == null || !p.equals(job.getInputDirectory()
			.resolve(Constants.CONFIG_YAML));
	}

	private BenchmarkJob convertJob(Job job) {
		return new BenchmarkJob(job);
	}

	private static JobSettings getJobSettings(int numberOfNodes,
		int haasTemplateId)
	{
		return new JobSettingsBuilder().jobName(HAAS_JOB_NAME).clusterNodeType(
			getHaasClusterNodeType()).templateId(haasTemplateId).walltimeLimit(
				getWalltime()).numberOfCoresPerNode(CORES_PER_NODE).numberOfNodes(
					numberOfNodes).build();
	}

	private static HaaSClientSettings constructSettingsFromParams(
		BenchmarkSPIMParameters params)
	{
		return new HaaSClientSettings() {

			@Override
			public String getUserName() {
				return params.username();
			}

			@Override
			public String getProjectId() {
				return Configuration.getHaasProjectID();
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
