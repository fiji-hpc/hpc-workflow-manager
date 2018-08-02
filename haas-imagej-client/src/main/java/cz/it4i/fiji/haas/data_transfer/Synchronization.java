
package cz.it4i.fiji.haas.data_transfer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.FileTransferInfo;
import cz.it4i.fiji.haas_java_client.FileTransferState;
import cz.it4i.fiji.haas_java_client.HaaSFileTransfer;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import cz.it4i.fiji.haas_java_client.UploadingFile;
import cz.it4i.fiji.haas_java_client.UploadingFileImpl;

public class Synchronization implements Closeable {

	public static final Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.data_transfer.Synchronization.class);

	private static final String FILE_INDEX_TO_UPLOAD_FILENAME = ".toUploadFiles";
	private static final String FILE_INDEX_UPLOADED_FILENAME = ".uploaded";
	private static final String FILE_INDEX_TO_DOWNLOAD_FILENAME =
		".toDownloadFiles";
	private static final String FILE_INDEX_DOWNLOADED_FILENAME = ".downloaded";

	private final Path workingDirectory;

	private final Path inputDirectory;

	private final Path outputDirectory;

	private final PersistentIndex<Path> filesDownloaded;

	private final PersistentIndex<Path> filesUploaded;

	private final PersistentSynchronizationProcess<Path> uploadProcess;

	private final P_PersistentDownloadProcess downloadProcess;

	private final ExecutorService service;

	private final Predicate<Path> uploadFilter;

	public Synchronization(Supplier<HaaSFileTransfer> fileTransferSupplier, Path workingDirectory, Path inputDirectory,
			Path outputDirectory, Runnable uploadFinishedNotifier, Runnable downloadFinishedNotifier, Predicate<Path> uploadFilter)
			throws IOException {
		this.workingDirectory = workingDirectory;
		this.inputDirectory = inputDirectory;
		this.outputDirectory = outputDirectory;
		this.service = Executors.newFixedThreadPool(2);
		this.filesDownloaded = new PersistentIndex<>(workingDirectory.resolve(
			FILE_INDEX_DOWNLOADED_FILENAME), name -> Paths.get(name));
		this.filesUploaded = new PersistentIndex<>(workingDirectory.resolve(
			FILE_INDEX_UPLOADED_FILENAME), name -> Paths.get(name));
		this.uploadProcess = createUploadProcess(fileTransferSupplier, service,
			uploadFinishedNotifier);
		this.downloadProcess = createDownloadProcess(fileTransferSupplier, service,
			downloadFinishedNotifier);
		this.uploadFilter = uploadFilter;
	}

	public synchronized void setUploadNotifier(ProgressNotifier notifier) {
		uploadProcess.setNotifier(notifier);
	}

	public void setDownloadNotifier(ProgressNotifier notifier) {
		downloadProcess.setNotifier(notifier);

	}

	public synchronized void startUpload() throws IOException {
		uploadProcess.start();
	}

	public void stopUpload() throws IOException {
		uploadProcess.stop();
	}

	public void resumeUpload() {
		uploadProcess.resume();
	}

	public synchronized CompletableFuture<?> startDownload(Collection<String> files) throws IOException {
		this.downloadProcess.setItems(files);
		return this.downloadProcess.start();
	}

	public synchronized void stopDownload() throws IOException {
		this.downloadProcess.stop();
	}

	public synchronized void resumeDownload() {
		this.downloadProcess.resume();
	}

	public List<FileTransferInfo> getFileTransferInfo() {
		final List<FileTransferInfo> list = new LinkedList<>();
		filesUploaded.getIndexedItems().forEach(ii -> {
			list.add(new FileTransferInfo(ii, FileTransferState.Finished));
		});
		uploadProcess.getIndexedItems().forEach(ii -> {
			list.add(new FileTransferInfo(ii, FileTransferState.Queuing));
		});
		return list;
	}

	@Override
	public void close() {
		service.shutdown();
		uploadProcess.shutdown();
		downloadProcess.shutdown();
	}

	private boolean canUpload(Path file) {
		return uploadFilter.test(file) && !file.getFileName().toString().matches("[.][^.]+")
				&& !filesDownloaded.contains(file);
	}

	private PersistentSynchronizationProcess<Path> createUploadProcess(Supplier<HaaSFileTransfer> fileTransferSupplier,
			ExecutorService executorService, Runnable uploadFinishedNotifier) throws IOException {
		return new PersistentSynchronizationProcess<Path>(executorService, fileTransferSupplier, uploadFinishedNotifier,
				workingDirectory.resolve(FILE_INDEX_TO_UPLOAD_FILENAME), name -> inputDirectory.resolve(name)) {

			@Override
			protected Collection<Path> getItems() throws IOException {
				try (DirectoryStream<Path> ds = Files.newDirectoryStream(inputDirectory,
						Synchronization.this::canUpload)) {
					return StreamSupport.stream(ds.spliterator(), false).filter(
						p -> !filesUploaded.contains(p)).collect(Collectors.toList());
				}

			}

			@Override
			protected void processItem(final HaaSFileTransfer tr, final Path p)
				throws InterruptedIOException
			{
				final UploadingFile uf = new UploadingFileImpl(p);
				tr.upload(uf);
				filesUploaded.insert(inputDirectory.resolve(p.toString()));
				try {
					filesUploaded.storeToWorkingFile();
				}
				catch (final IOException e) {
					log.error(e.getMessage(), e);
				}
			}

			@Override
			protected long getTotalSize(Iterable<Path> items, HaaSFileTransfer tr) {
				return StreamSupport.stream(items.spliterator(), false).map(p -> {
					try {
						return Files.size(p);
					} catch (IOException e) {
						log.error(e.getMessage(), e);
						return 0;
					}
				}).collect(Collectors.summingLong(val -> val.longValue()));
			}
		};
	}

	private P_PersistentDownloadProcess createDownloadProcess(Supplier<HaaSFileTransfer> fileTransferSupplier,
			ExecutorService executorService, Runnable uploadFinishedNotifier) throws IOException {

		return new P_PersistentDownloadProcess(executorService, fileTransferSupplier, uploadFinishedNotifier);
	}

	private class P_PersistentDownloadProcess extends PersistentSynchronizationProcess<String> {

		private Collection<String> items = Collections.emptyList();

		public P_PersistentDownloadProcess(ExecutorService service, Supplier<HaaSFileTransfer> fileTransferSupplier,
				Runnable processFinishedNotifier) throws IOException {
			super(service, fileTransferSupplier, processFinishedNotifier,
					workingDirectory.resolve(FILE_INDEX_TO_DOWNLOAD_FILENAME), name -> name);
		}

		private synchronized void setItems(Collection<String> items) {
			this.items = new LinkedList<>(items);
		}

		@Override
		protected synchronized Collection<String> getItems() throws IOException {
			return items;
		}

		@Override
		protected void processItem(final HaaSFileTransfer tr, final String file)
			throws InterruptedIOException
		{
			tr.download(file, outputDirectory);
			filesDownloaded.insert(outputDirectory.resolve(file));
			try {
				filesDownloaded.storeToWorkingFile();
			}
			catch (final IOException e) {
				log.error(e.getMessage(), e);
			}
		}

		@Override
		protected long getTotalSize(Iterable<String> files, HaaSFileTransfer tr) throws InterruptedIOException {
			return tr.obtainSize(StreamSupport.stream(files.spliterator(), false).collect(Collectors.toList())).stream()
					.collect(Collectors.summingLong(val -> val));
		}

	}
}
