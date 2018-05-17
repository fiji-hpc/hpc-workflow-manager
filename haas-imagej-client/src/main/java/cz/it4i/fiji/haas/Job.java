package cz.it4i.fiji.haas;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.JobManager.JobManager4Job;
import cz.it4i.fiji.haas.JobManager.JobSynchronizableFile;
import cz.it4i.fiji.haas.data_transfer.Synchronization;
import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.HaaSFileTransfer;
import cz.it4i.fiji.haas_java_client.JobInfo;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import cz.it4i.fiji.haas_java_client.TransferFileProgressForHaaSClient;
import cz.it4i.fiji.haas_java_client.UploadingFile;
import cz.it4i.fiji.scpclient.TransferFileProgress;
/***
 * TASK - napojit na UI 
 * @author koz01
 *
 */
public class Job {

	private static final String JOB_NAME = "job.name";

	private static final String JOB_NEEDS_UPLOAD = "job.needs_upload";

	private static final String JOB_INFO_FILENAME = ".jobinfo";

	private static final String JOB_NEEDS_DOWNLOAD = "job.needs_download";
	
	private static final String JOB_CAN_BE_DOWNLOADED = "job.can_be_downloaded";
	
	private static final String JOB_IS_DOWNLOADED = "job.downloaded";
	
	private static final String JOB_IS_UPLOADED = "job.uploaded";

	public static boolean isValidJobPath(Path path) {
		try {
			getJobId(path);
		} catch (NumberFormatException e) {
			return false;
		}
		return Files.isRegularFile(path.resolve(JOB_INFO_FILENAME));
	}

	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.Job.class);

	private Path jobDir;

	private final Supplier<HaaSClient> haasClientSupplier;

	private JobInfo jobInfo;
	
	private Long jobId;
	
	private PropertyHolder propertyHolder;
	
	private final JobManager4Job jobManager;
	
	private Synchronization synchronization;
	
	
	public Job(JobManager4Job jobManager, String name, Path basePath, Supplier<HaaSClient> haasClientSupplier)
			throws IOException {
		this(jobManager, haasClientSupplier);
		HaaSClient client = getHaaSClient();
		long id = client.createJob(name, Collections.emptyList());
		setJobDirectory(basePath.resolve("" + id));
		propertyHolder = new PropertyHolder(jobDir.resolve(JOB_INFO_FILENAME));
		Files.createDirectory(jobDir);
		setName(name);
		
	}

	public Job(JobManager4Job jobManager, Path jobDirectory, Supplier<HaaSClient> haasClientSupplier) {
		this(jobManager, haasClientSupplier);
		setJobDirectory(jobDirectory);
		propertyHolder = new PropertyHolder(jobDir.resolve(JOB_INFO_FILENAME));
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

	public void startDownload(Predicate<String> predicate) throws IOException {
		setProperty(JOB_NEEDS_DOWNLOAD, true);
		Collection<String> files = getHaaSClient().getChangedFiles(jobId).stream().filter(predicate)
				.collect(Collectors.toList());
		synchronization.startDownload(files);
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
		return Boolean.parseBoolean(getProperty(JOB_CAN_BE_DOWNLOADED));
	}
	
	public void setUploaded(boolean b) {
		setProperty(JOB_IS_UPLOADED, b);
	}

	public void setDownloaded(boolean b) {
		setProperty(JOB_IS_DOWNLOADED, b);
	}
	
	public boolean isUploaded() {
		return  getSafeBoolean(getProperty(JOB_IS_UPLOADED));
	}

	public boolean isDownloaded() {
		return getSafeBoolean(getProperty(JOB_IS_DOWNLOADED));
	}
	
	public boolean needsDownload() {
		return Boolean.parseBoolean(getProperty(JOB_NEEDS_DOWNLOAD));
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

	public Path storeDataInWorkdirectory(UploadingFile uploadingFile) throws IOException {
		Path result;
		try (InputStream is = uploadingFile.getInputStream()) {
			Files.copy(is, result = jobDir.resolve(uploadingFile.getName()));
		}
		return result;
	}

	synchronized public void download(Predicate<String> predicate, ProgressNotifier notifier) {
		List<String> files = getHaaSClient().getChangedFiles(jobId).stream().filter(predicate).collect(Collectors.toList());
		try (HaaSFileTransfer transfer =  haasClientSupplier.get().startFileTransfer(getId(), HaaSClient.DUMMY_TRANSFER_FILE_PROGRESS)) {
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

	public void setProperty(String jobNeedsUpload, boolean b) {
		propertyHolder.setValue(jobNeedsUpload, "" + b);
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

	public boolean remove() {
		boolean result;
		if ((result = jobManager.remove(this)) && Files.isDirectory(jobDir)) {
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

	private boolean getSafeBoolean(String value) {
		return value != null ? Boolean.parseBoolean(value) : false;
	}

	private void setJobDirectory(Path jobDirectory) {
		this.jobDir = jobDirectory;
		try {
			this.synchronization = new Synchronization(() -> startFileTransfer(HaaSClient.DUMMY_TRANSFER_FILE_PROGRESS),
					jobDir, () -> {
						setProperty(JOB_NEEDS_UPLOAD, false);
						setUploaded(true);
					}, () -> {
						setDownloaded(true);
						setProperty(JOB_NEEDS_DOWNLOAD, false);
						setCanBeDownloaded(false);
					});
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}



	private HaaSFileTransfer startFileTransfer( TransferFileProgress progress) {
		return haasClientSupplier.get().startFileTransfer(getId(), progress);
	}

	

	private void setName(String name) {
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

}
