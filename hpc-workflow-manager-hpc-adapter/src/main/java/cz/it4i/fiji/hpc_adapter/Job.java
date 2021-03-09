
package cz.it4i.fiji.hpc_adapter;

import static cz.it4i.fiji.hpc_client.Notifiers.emptyTransferFileProgress;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.hpc_adapter.HPCClientProxyAdapter.JobSubmission;
import cz.it4i.fiji.hpc_adapter.JobManager.JobManager4Job;
import cz.it4i.fiji.hpc_adapter.JobManager.JobSynchronizableFile;
import cz.it4i.fiji.hpc_client.HPCClientException;
import cz.it4i.fiji.hpc_client.HPCFileTransfer;
import cz.it4i.fiji.hpc_client.JobFileContent;
import cz.it4i.fiji.hpc_client.JobInfo;
import cz.it4i.fiji.hpc_client.JobState;
import cz.it4i.fiji.hpc_client.ProgressNotifier;
import cz.it4i.fiji.hpc_client.SynchronizableFile;
import cz.it4i.fiji.hpc_client.UploadingFile;
import cz.it4i.fiji.hpc_client.data_transfer.FileTransferInfo;
import cz.it4i.fiji.hpc_client.data_transfer.Synchronization;
import cz.it4i.fiji.hpc_client.data_transfer.TransferFileProgressForHPCClient;
import cz.it4i.fiji.scpclient.TransferFileProgress;

/***
 * TASK - napojit na UI
 * 
 * @author koz01
 */
public class Job {

	public static boolean isValidJobPath(Path path) {
		try {
			getJobId(path);
		}
		catch (NumberFormatException e) {
			return false;
		}
		return path.resolve(JOB_INFO_FILENAME).toFile().isFile();
	}

	private static final String JOB_NAME = "job.name";

	private static final String JOB_NEEDS_UPLOAD = "job.needs_upload";

	private static final String JOB_INFO_FILENAME = ".jobinfo";

	private static final String JOB_NEEDS_DOWNLOAD = "job.needs_download";

	private static final String JOB_CAN_BE_DOWNLOADED = "job.can_be_downloaded";

	private static final String JOB_IS_DOWNLOADED = "job.downloaded";

	private static final String JOB_IS_UPLOADED = "job.uploaded";

	private static final String JOB_OUTPUT_DIRECTORY_PATH =
		"job.output_directory_path";

	private static final String JOB_INPUT_DIRECTORY_PATH =
		"job.input_directory_path";

	private static final String USER_SCIPRT_NAME = "job.user_script_name";

	private static final String LAST_STARTED_TIMESTAMP =
		"job.last_started_timestamp";

	private static final String JOB_USE_DEMO_DATA = "job.use_demo_data";

	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_adapter.Job.class);

	private Path jobDir;

	private final HPCClientProxyAdapter<?> hpcClient;

	private JobInfo jobInfo;

	private Long jobId;

	private PropertyHolder propertyHolder;

	private final JobManager4Job jobManager;

	private Synchronization synchronization;

	private Path inputDirectory;

	private Path outputDirectory;

	private String userScriptName;

	private boolean useDemoData;

	private long lastStartedTimestamp = -1;

	public static Job submitNewJob(JobManager4Job jobManager, Path basePath,
		HPCClientProxyAdapter<? extends JobWithDirectorySettings> hpcClient,
		Object params) throws IOException
	{
		Job result = new Job(jobManager, hpcClient);
		JobSubmission<? extends JobWithDirectorySettings> jobSubmission = hpcClient
			.createJobSubmission(params);
		result.setJobDirectory(basePath.resolve("" + jobSubmission.getJobId()),
			jobSubmission.getJobSettings().getInputPath(), jobSubmission
				.getJobSettings().getOutputPath());
		result.propertyHolder = new PropertyHolder(result.jobDir.resolve(
			JOB_INFO_FILENAME));
		Files.createDirectory(result.jobDir);
		result.storeInputOutputDirectory();
		result.setName(jobSubmission.getJobSettings().getJobName());
		result.storeUserScriptName(jobSubmission.getJobSettings()
			.getUserScriptName());
		return result;
	}

	public static Job getExistingJob(JobManager4Job jobManager, Path jobDirectory,
		HPCClientProxyAdapter<?> hpcClient)
	{
		Job result = new Job(jobManager, hpcClient);
		result.propertyHolder = new PropertyHolder(jobDirectory.resolve(
			JOB_INFO_FILENAME));
		result.useDemoData = getSafeBoolean(result.propertyHolder.getValue(
			JOB_USE_DEMO_DATA));
		result.setJobDirectory(jobDirectory, jd -> result.useDemoData ? null
			: result.getDataDirectory(JOB_INPUT_DIRECTORY_PATH, jd), jd -> result
				.getDataDirectory(JOB_OUTPUT_DIRECTORY_PATH, jd));

		return result;
	}

	private Job(JobManager4Job jobManager, HPCClientProxyAdapter<?> hpcClient) {
		this.hpcClient = hpcClient;
		this.jobManager = jobManager;
	}

	public void startUploadData() {
		setProperty(JOB_NEEDS_UPLOAD, true);
		try {
			this.synchronization.startUpload();
		}
		catch (IOException e) {
			throw new HPCClientException(e);
		}
	}

	public void stopUploadData() {
		setProperty(JOB_NEEDS_UPLOAD, false);
		try {
			this.synchronization.stopUpload();
		}
		catch (IOException e) {
			throw new HPCClientException(e);
		}
	}

	public CompletableFuture<?> startDownload(Predicate<String> predicate)
		throws IOException
	{
		Collection<String> files = hpcClient.getChangedFiles(jobId).stream().filter(
			predicate).collect(Collectors.toList());
		if (files.isEmpty()) {
			return CompletableFuture.completedFuture(null);
		}
		setProperty(JOB_NEEDS_DOWNLOAD, true);
		return synchronization.startDownload(files);
	}

	public void stopDownloadData() {
		setProperty(JOB_NEEDS_DOWNLOAD, false);
		try {
			this.synchronization.stopDownload();
		}
		catch (IOException e) {
			throw new HPCClientException(e);
		}
	}

	public synchronized void resumeUpload() {
		if (needsUpload()) {
			synchronization.resumeUpload();
		}
	}

	public synchronized void resumeDownload() {
		if (needsDownload()) {
			synchronization.resumeDownload();
		}
	}

	public boolean canBeDownloaded() {
		return Boolean.parseBoolean(getProperty(JOB_CAN_BE_DOWNLOADED));
	}

	public void setUploaded(boolean b) {
		setProperty(JOB_IS_UPLOADED, b);
	}

	public void setDownloaded(boolean b) {
		setProperty(JOB_IS_DOWNLOADED, b);
	}

	public boolean isUploaded() {
		return getSafeBoolean(getProperty(JOB_IS_UPLOADED));
	}

	public boolean isUploading() {
		return synchronization.isUploading();
	}

	public boolean isDownloaded() {
		return getSafeBoolean(getProperty(JOB_IS_DOWNLOADED));
	}

	public boolean needsDownload() {
		return Boolean.parseBoolean(getProperty(JOB_NEEDS_DOWNLOAD));
	}

	public boolean isDownloading() {
		return synchronization.isDownloading();
	}

	public boolean needsUpload() {
		return Boolean.parseBoolean(getProperty(JOB_NEEDS_UPLOAD));
	}

	public void uploadFile(String file, ProgressNotifier notifier) {
		uploadFiles(Arrays.asList(file), notifier);
	}

	public void uploadFiles(Collection<String> filesNames,
		ProgressNotifier notifier)
	{
		Collection<UploadingFile> files = filesNames.stream().map(
			file -> getUploadingFile(jobDir.resolve(file))).collect(Collectors
				.toList());
		List<Long> totalSizes = files.stream().map(f -> {
			try {
				return f.getLength();
			}
			catch (IOException exception) {
				throw new HPCClientException(exception);
			}
		}).collect(Collectors.toList());
		long totalSize = totalSizes.stream().mapToLong(Long::longValue).sum();
		TransferFileProgressForHPCClient progress =
			new TransferFileProgressForHPCClient(totalSize, notifier);

		HPCFileTransfer transfer = hpcClient.startFileTransfer(getId(), progress);
		int index = 0;
		for (UploadingFile file : files) {
			String item;
			progress.startNewFile(totalSizes.get(index));
			item = "Uploading file: " + file.getName();
			notifier.addItem(item);
			try {
				transfer.upload(file);
			}
			catch (InterruptedIOException e) {
				notifier.itemDone(item);
				return;
			}
			notifier.itemDone(item);
			index++;
		}
	}

	public void submit() {
		hpcClient.submitJob(jobId);
		stopDownloadData();
		setCanBeDownloaded(true);
	}

	public synchronized long getId() {
		if (jobId == null) {
			jobId = getJobId(jobDir);
		}
		return jobId;
	}

	public boolean isUseDemoData() {
		return useDemoData;
	}

	public Path storeDataInWorkdirectory(UploadingFile uploadingFile)
		throws IOException
	{
		Path result;
		try (InputStream is = uploadingFile.getInputStream()) {
			result = jobDir.resolve(uploadingFile.getName());
			Files.copy(is, result);
		}
		return result;
	}

	public synchronized void download(Predicate<String> predicate,
		ProgressNotifier notifier)
	{
		List<String> files = hpcClient.getChangedFiles(jobId).stream().filter(
			predicate).collect(Collectors.toList());
		HPCFileTransfer transfer = hpcClient.startFileTransfer(getId(),
			emptyTransferFileProgress());

		List<Long> fileSizes;
		try {
			fileSizes = transfer.obtainSize(files);
		}
		catch (InterruptedIOException e1) {
			return;
		}
		final long totalFileSize = fileSizes.stream().mapToLong(Long::longValue)
			.sum();
		TransferFileProgressForHPCClient progress =
			new TransferFileProgressForHPCClient(totalFileSize, notifier);
		transfer.setProgress(progress);
		int idx = 0;
		for (String fileName : files) {
			String item;
			item = fileName;
			progress.addItem(item);
			progress.startNewFile(fileSizes.get(idx));
			try {
				transfer.download(fileName, jobDir);
			}
			catch (InterruptedIOException e) {
				progress.itemDone(item);
				return;
			}
			progress.itemDone(item);
			idx++;
		}
	}

	public JobState getState() {
		return getJobInfo().getState();
	}

	public Calendar getStartTime() {
		return jobInfo.getStartTime();
	}

	public Calendar getCreationTime() {
		return jobInfo.getCreationTime();
	}

	public Calendar getEndTime() {
		return jobInfo.getEndTime();
	}

	public List<String> getOutput(Collection<JobSynchronizableFile> output) {
		List<String> result = new ArrayList<>();

		// Check if there are tasks:
		boolean thereAreTasks = !getJobInfo().getTasks().isEmpty();
		if (thereAreTasks) {
			long taskId = (Long) getJobInfo().getTasks().toArray()[0];
			List<SynchronizableFile> synchronizableFiles = output.stream().map(
				file -> new SynchronizableFile(taskId, file.getType(), file
					.getOffset())).collect(Collectors.toList());

			result = hpcClient.downloadPartsOfJobFiles(jobId, synchronizableFiles)
				.stream().map(JobFileContent::getContent).collect(Collectors.toList());
		}

		return result;
	}

	public InputStream openLocalFile(String name) throws IOException {
		return Files.newInputStream(jobDir.resolve(name));
	}

	public synchronized void setProperty(String name, String value) {
		propertyHolder.setValue(name, value);
	}

	public void setProperty(String name, boolean value) {
		propertyHolder.setValue(name, "" + value);
	}

	public void setProperty(String name, int value) {
		propertyHolder.setValue(name, "" + value);
	}

	public void setProperty(String name, long value) {
		propertyHolder.setValue(name, "" + value);
	}

	public String getProperty(String name) {
		return propertyHolder.getValue(name);
	}

	public void updateInfo() {
		updateJobInfo();
	}

	public Path getDirectory() {
		return jobDir;
	}

	public boolean delete() {
		boolean result = jobManager.deleteJob(this);
		if ((result) && jobDir.toFile().isDirectory()) {
			List<Path> pathsToDelete;
			try (Stream<Path> dirStream = Files.walk(jobDir)) {
				pathsToDelete = dirStream.sorted(Comparator.reverseOrder()).collect(
					Collectors.toList());
				for (Path path : pathsToDelete) {
					Files.deleteIfExists(path);
				}
			}
			catch (IOException e) {
				log.error(e.getMessage(), e);
			}

		}
		return result;
	}

	public Collection<String> getChangedFiles() {
		return hpcClient.getChangedFiles(getId());
	}

	public void cancelJob() {
		hpcClient.cancelJob(jobId);
	}

	public List<Long> getFileSizes(List<String> names) {

		HPCFileTransfer transfer = hpcClient.startFileTransfer(getId(),
			emptyTransferFileProgress());

		try {
			return transfer.obtainSize(names);
		}
		catch (InterruptedIOException e) {
			return Collections.emptyList();
		}

	}

	public List<String> getFileContents(List<String> logs) {
		HPCFileTransfer transfer = hpcClient.startFileTransfer(getId(),
			emptyTransferFileProgress());
		return transfer.getContent(logs);
	}

	public void setDownloadNotifier(ProgressNotifier notifier) {
		synchronization.setDownloadNotifier(notifier);
	}

	public void setUploadNotifier(ProgressNotifier notifier) {
		synchronization.setUploadNotifier(notifier);
	}

	public void close() {
		synchronization.close();
	}

	public Path getInputDirectory() {
		return inputDirectory;
	}

	public Path getOutputDirectory() {
		return outputDirectory;
	}

	public List<FileTransferInfo> getFileTransferInfo() {
		return synchronization.getFileTransferInfo();
	}

	public String getUserScriptName() {
		if (userScriptName == null) {
			userScriptName = propertyHolder.getValue(USER_SCIPRT_NAME);
		}
		return userScriptName;
	}

	// Set the timestamp of the last started job in the job settings,
	// this is done in order to filter out older progress logs that might be
	// on the server.
	public void setLastStartedTimestamp() {
		this.lastStartedTimestamp = Instant.now().toEpochMilli();
		storeLastStartedTimestamp(this.lastStartedTimestamp);
	}

	public long getLastStartedTimestamp() {
		// If it is not set get the property from the configuration file ".jobinfo"
		if (lastStartedTimestamp == -1) {
			lastStartedTimestamp = Long.parseLong(propertyHolder.getValue(
				LAST_STARTED_TIMESTAMP));
		}
		return lastStartedTimestamp;
	}

	public void createEmptyFile(String fileName) throws InterruptedIOException {
		HPCFileTransfer transfer = hpcClient.startFileTransfer(getId());
		transfer.upload(new EmptyUploadingFile(fileName));
	}

	private void storeInputOutputDirectory() {
		if (inputDirectory == null) {
			useDemoData = true;
			propertyHolder.setValue(JOB_USE_DEMO_DATA, "" + useDemoData);
		}
		else {
			storeDataDirectory(JOB_INPUT_DIRECTORY_PATH, inputDirectory);
		}
		storeDataDirectory(JOB_OUTPUT_DIRECTORY_PATH, outputDirectory);
	}

	private void storeUserScriptName(String newUserScriptName) {
		propertyHolder.setValue(USER_SCIPRT_NAME, newUserScriptName);
	}

	private void storeDataDirectory(final String directoryPropertyName,
		final Path directory)
	{
		if (!jobDir.equals(directory)) {
			propertyHolder.setValue(directoryPropertyName, directory.toString());
		}
	}

	private Path getDataDirectory(final String typeOfDirectory,
		final Path jobDirectory)
	{
		final String directory = propertyHolder.getValue(typeOfDirectory);
		return directory != null ? Paths.get(directory) : jobDirectory;
	}

	private void setJobDirectory(final Path jobDirectory,
		final UnaryOperator<Path> inputDirectoryProvider,
		final UnaryOperator<Path> outputDirectoryProvider)
	{
		this.jobDir = jobDirectory;

		try {
			this.inputDirectory = inputDirectoryProvider.apply(jobDir);
			this.outputDirectory = outputDirectoryProvider.apply(jobDir);
			this.synchronization = new Synchronization(() -> startFileTransfer(
				emptyTransferFileProgress()), jobDir, this.inputDirectory,
				this.outputDirectory, () -> {
					setProperty(JOB_NEEDS_UPLOAD, false);
					setUploaded(true);
				}, () -> {
					setDownloaded(true);
					setProperty(JOB_NEEDS_DOWNLOAD, false);
					setCanBeDownloaded(false);
				}, p -> jobManager.canUpload(Job.this, p));
		}
		catch (final IOException e) {
			throw new HPCClientException(e);
		}
	}

	private HPCFileTransfer startFileTransfer(
		final TransferFileProgress progress)
	{
		return hpcClient.startFileTransfer(getId(), progress);
	}

	private void setName(final String name) {
		setProperty(JOB_NAME, name);
	}

	private JobInfo getJobInfo() {
		if (jobInfo == null) {
			updateJobInfo();
		}
		return jobInfo;
	}

	private void updateJobInfo() {
		jobInfo = hpcClient.obtainJobInfo(getId());
	}

	private void setCanBeDownloaded(boolean b) {
		setProperty(JOB_CAN_BE_DOWNLOADED, b);
	}

	private void storeLastStartedTimestamp(long timestamp) {
		propertyHolder.setValue(LAST_STARTED_TIMESTAMP, Long.toString(timestamp));
	}

	private static long getJobId(Path path) {
		return Long.parseLong(path.getFileName().toString());
	}

	private static boolean getSafeBoolean(final String value) {
		return value != null && Boolean.parseBoolean(value);
	}

	private static UploadingFile getUploadingFile(final Path file) {
		return new UploadingFile() {

			@Override
			public InputStream getInputStream() {
				try {
					return Files.newInputStream(file);
				}
				catch (final IOException e) {
					throw new HPCClientException(e);
				}
			}

			@Override
			public String getName() {
				return file.getFileName().toString();
			}

			@Override
			public long getLength() {
				try {
					return Files.size(file);
				}
				catch (final IOException e) {
					throw new HPCClientException(e);
				}
			}

			@Override
			public long getLastTime() {
				try {
					return Files.getLastModifiedTime(file).toMillis();
				}
				catch (final IOException e) {
					throw new HPCClientException(e);
				}
			}

		};
	}

	public String getRemoteJobInfo() {
		return hpcClient.getRemoteJobInfo(getId());
	}
}
