package cz.it4i.fiji.haas.data_transfer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;
import cz.it4i.fiji.haas_java_client.HaaSFileTransfer;

public class Synchronization {

	public static final Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.data_transfer.Synchronization.class);
	
	private Supplier<HaaSFileTransfer> fileTransferSupplier;
	
	private Supplier<Iterable<String>> changedFilesSupplier;
	
	private Map<String,String> propertiesHolder;
	
	private Path workingDirectory;
	
	private Queue<Path> toUpload = new LinkedBlockingQueue<>();
	
	private ExecutorService service;
	
	private FileRepository fileRepository;
	
	
	void upload() throws IOException {
		try(DirectoryStream<Path> ds = Files.newDirectoryStream(workingDirectory,this::isNotHidden)) {
			for (Path file : ds) {
				if(needsUpload(file)) {
					toUpload.add(file);
					service.execute(this::doRun);
				}
			}
		}
	}

	private boolean needsUpload(Path file) {
		return fileRepository.needsDownload(file);
	}

	private boolean isNotHidden(Path file) {
		
		return !file.getFileName().toString().matches("[.][^.]+");
	}
	
	private void doRun() {
		try(HaaSFileTransfer tr = fileTransferSupplier.get()) {
			while (!toUpload.isEmpty()) {
				Path p = toUpload.poll();
				UploadingFile uf = createUploadingFile(p);
				tr.upload(Arrays.asList(uf));
				fileUploaded(p);
			}
		} finally {
			try {
				fileRepository.storeToFile();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private void fileUploaded(Path p) {
		fileRepository.uploaded(p);
	}

	private UploadingFile createUploadingFile(Path p) {
		// TODO Auto-generated method stub
		return null;
	}
}
