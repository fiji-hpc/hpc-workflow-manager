
package cz.it4i.fiji.haas;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.JobManager.JobManager4Job;
import cz.it4i.fiji.haas.JobManager.JobSynchronizableFile;
import cz.it4i.fiji.haas.data_transfer.Synchronization;
import cz.it4i.fiji.haas_java_client.FileTransferInfo;
import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.HaaSFileTransfer;
import cz.it4i.fiji.haas_java_client.JobInfo;
import cz.it4i.fiji.haas_java_client.JobSettings;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import cz.it4i.fiji.haas_java_client.TransferFileProgressForHaaSClient;
import cz.it4i.fiji.haas_java_client.UploadingFile;
import cz.it4i.fiji.haas_java_client.UploadingFileData;
import cz.it4i.fiji.scpclient.TransferFileProgress;

/***
 * TASK - napojit na UI
 * 
 * @author koz01
 *
 */
public class Job {

	public static boolean isValidJobPath(Path path) {
		try {
			getJobId(path);
		} catch (NumberFormatException e) {
			return false;
		}
		return Files.isRegularFile(path.resolve(JOB_INFO_FILENAME));
	}

	private static final String JOB_NAME = "job.name";

	private static final String JOB_NEEDS_UPLOAD = "job.needs_upload";

	private static final String JOB_INFO_FILENAME = ".jobinfo";

	private static final String JOB_NEEDS_DOWNLOAD = "job.needs_download";

	private static final String JOB_CAN_BE_DOWNLOADED = "job.can_be_downloaded";

	private static final String JOB_IS_DOWNLOADED = "job.downloaded";

	private static final String JOB_IS_UPLOADED = "job.uploaded";
	
	private static final String JOB_HAAS_TEMPLATE_ID = "job.haas_template_id";

	private static final String JOB_OUTPUT_DIRECTORY_PATH = "job.output_directory_path";

	private static final String JOB_INPUT_DIRECTORY_PATH = "job.input_directory_path";

	private static final String JOB_USE_DEMO_DATA = "job.use_demo_data";

	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.Job.class);

	private Path jobDir;

	private final Supplier<HaaSClient> haasClientSupplier;

	private JobInfo jobInfo;

	private Long jobId;

	private PropertyHolder propertyHolder;

	private final JobManager4Job jobManager;

	private Synchronization synchronization;

	private Path inputDirectory;

	private Path outputDirectory;

	private boolean useDemoData;

	

	public Job(JobManager4Job jobManager, JobSettings jobSettings, Path basePath, Supplier<HaaSClient> haasClientSupplier,
			Function<Path, Path> inputDirectoryProvider, Function<Path, Path> outputDirectoryProvider)
			throws IOException {
		this(jobManager, haasClientSupplier);
		HaaSClient client = getHaaSClient();
		long id = client.createJob(jobSettings, Collections.emptyList());
		setJobDirectory(basePath.resolve("" + id), inputDirectoryProvider, outputDirectoryProvider);
		propertyHolder = new PropertyHolder(jobDir.resolve(JOB_INFO_FILENAME));
		Files.createDirectory(this.jobDir);
		storeInputOutputDirectory();
		setName(jobSettings.getJobName());
		setHaasTemplateId(jobSettings.getTemplateId());
	}

	public Job(JobManager4Job jobManager, Path jobDirectory, Supplier<HaaSClient> haasClientSupplier) {
		this(jobManager, haasClientSupplier);
		propertyHolder = new PropertyHolder(jobDirectory.resolve(JOB_INFO_FILENAME));
		useDemoData = getSafeBoolean(propertyHolder.getValue(JOB_USE_DEMO_DATA));
		setJobDirectory(jobDirectory, jd -> useDemoData ? null : getDataDirectory(JOB_INPUT_DIRECTORY_PATH, jd),
				jd -> getDataDirectory(JOB_OUTPUT_DIRECTORY_PATH, jd));
	}

	private Job(JobManager4Job jobManager, Supplier<HaaSClient> haasClientSupplier) {
		this.haasClientSupplier = haasClientSupplier;
		this.jobManager = jobManager;
	}

	public void startUploadData() {
		setProperty(JOB_NEEDS_UPLOAD, true);
		try {
			this.synchronization.startUpload();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public void stopUploadData() {
		setProperty(JOB_NEEDS_UPLOAD, false);
		try {
			this.synchronization.stopUpload();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public CompletableFuture<?> startDownload(Predicate<String> predicate) throws IOException {
		Collection<String> files = getHaaSClient().getChangedFiles(jobId).stream().filter(predicate)
				.collect(Collectors.toList());
		if(files.isEmpty()) {
			return CompletableFuture.completedFuture(null);
		}
		setProperty(JOB_NEEDS_DOWNLOAD, true);
		return synchronization.startDownload(files);
	}

	public void stopDownloadData() {
		setProperty(JOB_NEEDS_DOWNLOAD, false);
		try {
			this.synchronization.stopDownload();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
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
		return true;//Boolean.parseBoolean(getProperty(JOB_CAN_BE_DOWNLOADED));
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

	public void uploadFiles(Collection<String> filesNames, ProgressNotifier notifier) {
		Collection<UploadingFile> files = filesNames.stream()
				.map(file -> HaaSClient.getUploadingFile(jobDir.resolve(file))).collect(Collectors.toList());
		List<Long> totalSizes = files.stream().map(f -> {
			try {
				return f.getLength();
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}).collect(Collectors.toList());
		long totalSize = totalSizes.stream().mapToLong(l -> l.longValue()).sum();
		TransferFileProgressForHaaSClient progress = new TransferFileProgressForHaaSClient(totalSize, notifier);

		HaaSClient client = getHaaSClient();
		try (HaaSFileTransfer transfer = client.startFileTransfer(getId(), progress)) {
			int index = 0;
			for (UploadingFile file : files) {
				String item;
				progress.startNewFile(totalSizes.get(index));
				notifier.addItem(item = "Uploading file: " + file.getName());
				try {
					transfer.upload(file);
				} catch (InterruptedIOException e) {
					notifier.itemDone(item);
					return;
				}
				notifier.itemDone(item);
				index++;
			}
		}
	}

	public void submit() {
		HaaSClient client = getHaaSClient();
		client.submitJob(jobId);
		stopDownloadData();
		setCanBeDownloaded(true);
	}

	synchronized public long getId() {
		if (jobId == null) {
			jobId = getJobId(jobDir);
		}
		return jobId;
	}
	
	public boolean isUseDemoData() {
		return useDemoData;
	}

	public Path storeDataInWorkdirectory(UploadingFile uploadingFile) throws IOException {
		Path result;
		try (InputStream is = uploadingFile.getInputStream()) {
			Files.copy(is, result = jobDir.resolve(uploadingFile.getName()));
		}
		return result;
	}

	synchronized public void download(Predicate<String> predicate, ProgressNotifier notifier) {
		List<String> files = getHaaSClient().getChangedFiles(jobId).stream().filter(predicate)
				.collect(Collectors.toList());
		try (HaaSFileTransfer transfer = haasClientSupplier.get().startFileTransfer(getId(),
				HaaSClient.DUMMY_TRANSFER_FILE_PROGRESS)) {
			List<Long> fileSizes;
			try {
				fileSizes = transfer.obtainSize(files);
			} catch (InterruptedIOException e1) {
				return;
			}
			final long totalFileSize = fileSizes.stream().mapToLong(i -> i.longValue()).sum();
			TransferFileProgressForHaaSClient progress = new TransferFileProgressForHaaSClient(totalFileSize, notifier);
			transfer.setProgress(progress);
			int idx = 0;
			for (String fileName : files) {
				String item;
				progress.addItem(item = fileName);
				progress.startNewFile(fileSizes.get(idx));
				try {
					transfer.download(fileName, jobDir);
				} catch (InterruptedIOException e) {
					progress.itemDone(item);
					return;
				}
				progress.itemDone(item);
				idx++;
			}
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

	public List<String> getOutput(Iterable<JobSynchronizableFile> output) {
		HaaSClient.SynchronizableFiles taskFileOffset = new HaaSClient.SynchronizableFiles();
		long taskId = (Long) getJobInfo().getTasks().toArray()[0];
		output.forEach(file -> taskFileOffset.addFile(taskId, file.getType(), file.getOffset()));
		return getHaaSClient().downloadPartsOfJobFiles(jobId, taskFileOffset).stream().map(f -> f.getContent())
				.collect(Collectors.toList());
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
		boolean result;
		if ((result = jobManager.deleteJob(this)) && Files.isDirectory(jobDir)) {
			List<Path> pathsToDelete;
			try {
				pathsToDelete = Files.walk(jobDir).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
				for (Path path : pathsToDelete) {
					Files.deleteIfExists(path);
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}

		}
		return result;
	}

	public Collection<String> getChangedFiles() {
		return getHaaSClient().getChangedFiles(getId());
	}

	public void cancelJob() {
		getHaaSClient().cancelJob(jobId);
	}

	public List<Long> getFileSizes(List<String> names) {

		try (HaaSFileTransfer transfer = getHaaSClient().startFileTransfer(getId(),
				HaaSClient.DUMMY_TRANSFER_FILE_PROGRESS)) {
			try {
				return transfer.obtainSize(names);
			} catch (InterruptedIOException e) {
				return Collections.emptyList();
			}
		}
	}

	public List<String> getFileContents(List<String> logs) {
		try (HaaSFileTransfer transfer = getHaaSClient().startFileTransfer(getId(),
				HaaSClient.DUMMY_TRANSFER_FILE_PROGRESS)) {
			return transfer.getContent(logs);
		}
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

	public void createEmptyFile(String fileName) throws InterruptedIOException {
		try(HaaSFileTransfer transfer = haasClientSupplier.get().startFileTransfer(getId())) {
			transfer.upload(new UploadingFileData(fileName));
		}
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

	private boolean getSafeBoolean(final String value) {
		return value != null ? Boolean.parseBoolean(value) : false;
	}

	private void setJobDirectory(final Path jobDirectory,
		final Function<Path, Path> inputDirectoryProvider,
		final Function<Path, Path> outputDirectoryProvider)
	{
		this.jobDir = jobDirectory;

		try {
			this.synchronization = new Synchronization(() -> startFileTransfer(
				HaaSClient.DUMMY_TRANSFER_FILE_PROGRESS), jobDir, this.inputDirectory =
					inputDirectoryProvider.apply(jobDir), this.outputDirectory =
						outputDirectoryProvider.apply(jobDir), () -> {
							setProperty(JOB_NEEDS_UPLOAD, false);
							setUploaded(true);
						}, () -> {
							setDownloaded(true);
							setProperty(JOB_NEEDS_DOWNLOAD, false);
							setCanBeDownloaded(false);
						}, p -> jobManager.canUpload(Job.this, p));
		}
		catch (final IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private HaaSFileTransfer startFileTransfer(
		final TransferFileProgress progress)
	{
		return haasClientSupplier.get().startFileTransfer(getId(), progress);
	}

	private void setName(final String name) {
		setProperty(JOB_NAME, name);
	}

	private HaaSClient getHaaSClient() {
		return this.haasClientSupplier.get();
	}

	private JobInfo getJobInfo() {
		if (jobInfo == null) {
			updateJobInfo();
		}
		return jobInfo;
	}

	private void updateJobInfo() {
		jobInfo = getHaaSClient().obtainJobInfo(getId());
	}

	private static long getJobId(Path path) {
		return Long.parseLong(path.getFileName().toString());
	}

	private void setCanBeDownloaded(boolean b) {
		setProperty(JOB_CAN_BE_DOWNLOADED, b);
	}

	public void setHaasTemplateId(long id) {
		setProperty(JOB_HAAS_TEMPLATE_ID, id);
	}

	public long getHaasTemplateId() {
		return getSafeTemplateId(getProperty(JOB_HAAS_TEMPLATE_ID));
	}

	private long getSafeTemplateId(final String value) {
		if (value != null) {
			return Long.parseLong(value);
		} 
		
		return 4;  // FIXME enum defined somewhere else in SPIM_WORKFLOW(4) hpc-workflow-manager-client/src/main/java/cz/it4i/fiji/haas_spim_benchmark/ui/NewJobController.java
	}
	
}
