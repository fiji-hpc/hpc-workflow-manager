package cz.it4i.fiji.haas.data_transfer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;
import cz.it4i.fiji.haas_java_client.HaaSFileTransfer;
import cz.it4i.fiji.haas_java_client.UploadingFileImpl;

public class Synchronization {

	public static final Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.data_transfer.Synchronization.class);
	
	private static final String FILE_INDEX_TO_UPLOAD_FILENAME = ".toUploadFiles";
	
	private static final String FILE_INDEX_TO_DOWNLOAD_FILENAME = ".toDownloadFiles";
	
	private static final String FILE_INDEX_DOWNLOADED_FILENAME = ".downloaded";
	
	
	private Supplier<HaaSFileTransfer> fileTransferSupplier;
	
	private Path workingDirectory;
	
	private Queue<Path> toUpload = new LinkedBlockingQueue<>();
	
	private FileIndex filesToUpload;
	
	private FileIndex filesToDownload;
	
	private FileIndex filesDownloaded;
	
	private SimpleThreadRunner runnerForUpload;
	
	private boolean startUploadFinished;

	private Runnable uploadFinishedNotifier;
	
	public Synchronization(Supplier<HaaSFileTransfer> fileTransferSupplier, Path workingDirectory,
			ExecutorService service, Runnable uploadFinishedNotifier ) throws IOException {
		this.fileTransferSupplier = fileTransferSupplier;
		this.workingDirectory = workingDirectory;
		this.filesToUpload = new FileIndex(workingDirectory.resolve(FILE_INDEX_TO_UPLOAD_FILENAME));
		this.filesToDownload = new FileIndex(workingDirectory.resolve(FILE_INDEX_TO_DOWNLOAD_FILENAME));
		this.filesDownloaded = new FileIndex(workingDirectory.resolve(FILE_INDEX_DOWNLOADED_FILENAME));
		this.runnerForUpload = new SimpleThreadRunner(service);
		this.uploadFinishedNotifier = uploadFinishedNotifier;
	}

	public synchronized void startUpload() throws IOException {
		startUploadFinished = false;
		filesToUpload.clear();
		try(DirectoryStream<Path> ds = Files.newDirectoryStream(workingDirectory,this::isNotHidden)) {
			for (Path file : ds) {
				filesToUpload.insert(file);
				toUpload.add(file);
				runnerForUpload.runIfNotRunning(this::doUpload);
			}
		} finally {
			startUploadFinished = true;
			filesToUpload.storeToFile();
			
		}
	}
	
	public void stopUpload() throws IOException {
		toUpload.clear();
		filesToUpload.clear();
	}

	public void resumeUpload() {
		filesToUpload.fillQueue(toUpload);
		if(!toUpload.isEmpty()) {
			runnerForUpload.runIfNotRunning(this::doUpload);
		}
	}
	
	public synchronized void startDownload(Collection<String> files) throws IOException {
		filesToDownload.clear();
		
	}

	private boolean isNotHidden(Path file) {
		
		return !file.getFileName().toString().matches("[.][^.]+");
	}
	
	private void doUpload(AtomicBoolean reRun) {
		try(HaaSFileTransfer tr = fileTransferSupplier.get()) {
			while (!toUpload.isEmpty()) {
				Path p = toUpload.poll();
				UploadingFile uf = createUploadingFile(p);
				log.info("upload: " + p);
				tr.upload(uf);
				fileUploaded(p);
				log.info("uploaded: " + p);
				reRun.set(false);
			}
		} finally {
			synchronized (this) {
				if (startUploadFinished) {
					uploadFinishedNotifier.run();
				}
			}
		}
	}

	private void fileUploaded(Path p) {
		try {
			filesToUpload.uploaded(p);
			filesToUpload.storeToFile();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	private UploadingFile createUploadingFile(Path p) {
		return new UploadingFileImpl(p);
	}
}
