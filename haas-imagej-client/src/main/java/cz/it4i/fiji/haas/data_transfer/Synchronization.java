package cz.it4i.fiji.haas.data_transfer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;
import cz.it4i.fiji.haas_java_client.HaaSFileTransfer;

public class Synchronization {

	public static final Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.data_transfer.Synchronization.class);
	
	private Supplier<HaaSFileTransfer> fileTransferSupplier;
	
	private Path workingDirectory;
	
	private Queue<Path> toUpload = new LinkedBlockingQueue<>();
	
	private FileIndex fileRepository;
	
	private SimpleThreadRunner runnerForUpload;
	
	private boolean startUploadFinished;

	private Runnable uploadFinishedNotifier;
	
	public Synchronization(Supplier<HaaSFileTransfer> fileTransferSupplier, Path workingDirectory,
			ExecutorService service, Runnable uploadFinishedNotifier ) throws IOException {
		this.fileTransferSupplier = fileTransferSupplier;
		this.workingDirectory = workingDirectory;
		this.fileRepository = new FileIndex(workingDirectory);
		this.runnerForUpload = new SimpleThreadRunner(service);
		this.uploadFinishedNotifier = uploadFinishedNotifier;
	}

	public synchronized void startUpload() throws IOException {
		startUploadFinished = false;
		fileRepository.clear();
		try(DirectoryStream<Path> ds = Files.newDirectoryStream(workingDirectory,this::isNotHidden)) {
			for (Path file : ds) {
				fileRepository.needsDownload(file);
				toUpload.add(file);
				runnerForUpload.runIfNotRunning(this::doUpload);
			}
		} finally {
			startUploadFinished = true;
			fileRepository.storeToFile();
			
		}
	
	}

	public void stopUpload() throws IOException {
		toUpload.clear();
		fileRepository.clear();
	}

	public void resumeUpload() {
		fileRepository.fileUploadQueue(toUpload);
		if(!toUpload.isEmpty()) {
			runnerForUpload.runIfNotRunning(this::doUpload);
		}
	}

	private boolean isNotHidden(Path file) {
		
		return !file.getFileName().toString().matches("[.][^.]+");
	}
	
	private void doUpload(AtomicBoolean reRun) {
		try(HaaSFileTransfer tr = fileTransferSupplier.get()) {
			while (!toUpload.isEmpty()) {
				Path p = toUpload.poll();
				UploadingFile uf = createUploadingFile(p);
				tr.upload(Arrays.asList(uf));
				fileUploaded(p);
				reRun.set(false);
			}
		} finally {
			try {
				fileRepository.storeToFile();
				synchronized(this) {
					if(startUploadFinished) {
						uploadFinishedNotifier.run();
					}
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private void fileUploaded(Path p) {
		fileRepository.uploaded(p);
	}

	private UploadingFile createUploadingFile(Path p) {
		return new UploadingFileImpl(p);
	}
}
