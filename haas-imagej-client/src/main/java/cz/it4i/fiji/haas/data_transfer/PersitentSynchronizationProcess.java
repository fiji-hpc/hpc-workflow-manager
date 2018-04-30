package cz.it4i.fiji.haas.data_transfer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSFileTransfer;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import cz.it4i.fiji.haas_java_client.TransferFileProgressForHaaSClient;
import cz.it4i.fiji.scpclient.TransferFileProgress;

public abstract class PersitentSynchronizationProcess<T> {

	private boolean startFinished = true;
	
	public static final Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas.data_transfer.PersitentSynchronizationProcess.class);

	private static final TransferFileProgress DUMMY_FILE_PROGRESS =  new TransferFileProgress() {
		@Override
		public void dataTransfered(long bytesTransfered) {
		}
	};
	
	private PersistentIndex<T> index;
	
	private Queue<T> toProcessQueue = new LinkedBlockingQueue<T>();
	
	private SimpleThreadRunner runner;

	private Supplier<HaaSFileTransfer> fileTransferSupplier;

	private Runnable processFinishedNotifier;

	private String name;

	private ProgressNotifier notifier;
	
	public PersitentSynchronizationProcess(String name,ExecutorService service, Supplier<HaaSFileTransfer> fileTransferSupplier, Runnable processFinishedNotifier, Path indexFile,Function<String,T> convertor) throws IOException {
		runner = new SimpleThreadRunner(service);
		this.name = name;
		this.fileTransferSupplier = fileTransferSupplier;
		this.processFinishedNotifier = processFinishedNotifier;
		this.index = new PersistentIndex<>(indexFile, convertor);
	}
	
	public synchronized void start() throws IOException {
		startFinished = false;
		index.clear();
		try{
			for (T item : getItems()) {
				index.insert(item);
				toProcessQueue.add(item);
			}
			runner.runIfNotRunning(this::doProcess);	
		} finally {
			startFinished = true;
			index.storeToFile();
		}
	}
	
	

	public void stop() throws IOException {
		toProcessQueue.clear();
		index.clear();
	}

	public void resume() {
		index.fillQueue(toProcessQueue);
		runner.runIfNotRunning(this::doProcess);
	}
	
	abstract protected Iterable<T> getItems() throws IOException;
	
	abstract protected void processItem(HaaSFileTransfer tr, T p);
	
	private void doProcess(AtomicBoolean reRun) {
		try(HaaSFileTransfer tr = fileTransferSupplier.get()) {
			tr.setProgress(getTransferFileProgress(tr));
			while (!toProcessQueue.isEmpty()) {
				T p = toProcessQueue.poll();
				
				log.info(name + "ing: " + p);
				processItem(tr, p);
				fileUploaded(p);
				log.info(name + "ed: " + p);
				reRun.set(false);
			}
		} finally {
			synchronized (this) {
				if (startFinished) {
					processFinishedNotifier.run();
				}
			}
		}
	}

	abstract protected long getTotalSize(Iterable<T> items, HaaSFileTransfer tr);

	private void fileUploaded(T p) {
		try {
			index.remove(p);
			index.storeToFile();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setNotifier(ProgressNotifier notifier) {
		this.notifier = notifier;
	}

	private TransferFileProgress getTransferFileProgress(HaaSFileTransfer tr) {
		if(notifier == null) {
			return DUMMY_FILE_PROGRESS;
		}
		return new TransferFileProgressForHaaSClient(getTotalSize(toProcessQueue, tr), notifier);
	}

	
}
