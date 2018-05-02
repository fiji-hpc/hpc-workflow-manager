package cz.it4i.fiji.haas.data_transfer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.HaaSFileTransfer;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import cz.it4i.fiji.haas_java_client.TransferFileProgressForHaaSClient;

public abstract class PersistentSynchronizationProcess<T> {

	private boolean startFinished = true;

	public static final Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas.data_transfer.PersistentSynchronizationProcess.class);

	private static final TransferFileProgressForHaaSClient DUMMY_FILE_PROGRESS = new TransferFileProgressForHaaSClient(
			0, HaaSClient.DUMMY_PROGRESS_NOTIFIER);

	private PersistentIndex<T> index;

	private Queue<T> toProcessQueue = new LinkedBlockingQueue<T>();
	
	private Set<Thread> runningTransferThreads = Collections.synchronizedSet(new HashSet<>());

	private SimpleThreadRunner runner;

	private Supplier<HaaSFileTransfer> fileTransferSupplier;

	private Runnable processFinishedNotifier;

	private ProgressNotifier notifier;

	public PersistentSynchronizationProcess(ExecutorService service,
			Supplier<HaaSFileTransfer> fileTransferSupplier, Runnable processFinishedNotifier, Path indexFile,
			Function<String, T> convertor) throws IOException {
		runner = new SimpleThreadRunner(service);
		this.fileTransferSupplier = fileTransferSupplier;
		this.processFinishedNotifier = processFinishedNotifier;
		this.index = new PersistentIndex<>(indexFile, convertor);
	}

	public synchronized void start() throws IOException {
		startFinished = false;
		index.clear();
		try {
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
		notifier.setCount(-1, -1);
		runningTransferThreads.forEach(t -> t.interrupt());
	}

	public void resume() {
		index.fillQueue(toProcessQueue);
		runner.runIfNotRunning(this::doProcess);
	}

	abstract protected Iterable<T> getItems() throws IOException;

	abstract protected void processItem(HaaSFileTransfer tr, T p) throws InterruptedIOException;

	private void doProcess(AtomicBoolean reRun) {
		boolean interrupted = false;
		try (HaaSFileTransfer tr = fileTransferSupplier.get()) {
			TransferFileProgressForHaaSClient notifier;
			tr.setProgress(notifier = getTransferFileProgress(tr));
			runningTransferThreads.add(Thread.currentThread());
			while (!toProcessQueue.isEmpty()) {
				T p = toProcessQueue.poll();
				String item = p.toString(); 
				notifier.addItem(item);
				try {
					processItem(tr, p);
					fileUploaded(p);
				} catch (InterruptedIOException e) {
					toProcessQueue.clear();
					interrupted = true;
				}
				notifier.itemDone(item);
				reRun.set(false);
			}
			runningTransferThreads.remove(Thread.currentThread());
			notifier.done();
		} finally {
			synchronized (this) {
				if (startFinished && !interrupted && !Thread.interrupted()) {
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

	private TransferFileProgressForHaaSClient getTransferFileProgress(HaaSFileTransfer tr) {
		if (notifier == null) {
			return DUMMY_FILE_PROGRESS;
		}
		return new TransferFileProgressForHaaSClient(getTotalSize(toProcessQueue, tr), notifier);
	}

}
