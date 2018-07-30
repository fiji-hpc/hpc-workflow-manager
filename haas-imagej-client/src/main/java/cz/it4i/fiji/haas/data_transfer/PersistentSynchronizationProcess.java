package cz.it4i.fiji.haas.data_transfer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

	private final static String INIT_TRANSFER_ITEM = "init transfer";

	private final PersistentIndex<T> index;

	private final Queue<T> toProcessQueue = new LinkedBlockingQueue<>();

	private final Set<Thread> runningTransferThreads = Collections.synchronizedSet(new HashSet<>());

	private final SimpleThreadRunner runner;

	private final Supplier<HaaSFileTransfer> fileTransferSupplier;

	private final Runnable processFinishedNotifier;

	private ProgressNotifier notifier;

	private final AtomicInteger runningProcessCounter = new AtomicInteger();

	public PersistentSynchronizationProcess(ExecutorService service, Supplier<HaaSFileTransfer> fileTransferSupplier,
			Runnable processFinishedNotifier, Path indexFile, Function<String, T> convertor) throws IOException {
		runner = new SimpleThreadRunner(service);
		this.fileTransferSupplier = fileTransferSupplier;
		this.processFinishedNotifier = processFinishedNotifier;
		this.index = new PersistentIndex<>(indexFile, convertor);
	}

	public synchronized CompletableFuture<?> start() throws IOException {
		startFinished = false;
		index.clear();
		try {
			for (T item : getItems()) {
				index.insert(item);
				toProcessQueue.add(item);
			}
			return runner.runIfNotRunning(this::doProcess);
		} finally {
			startFinished = true;
			index.storeToWorkingFile();
		}
	}

	public void stop() throws IOException {
		toProcessQueue.clear();
		index.clear();
		notifyStop();
		runningTransferThreads.forEach(t -> t.interrupt());
	}

	public void shutdown() {
		toProcessQueue.clear();
		runningTransferThreads.forEach(t -> t.interrupt());
		waitForFinishAllProcesses();
	}

	public void resume() {
		toProcessQueue.addAll(index.getIndexedItems());
		runner.runIfNotRunning(this::doProcess);
	}

	public Set<T> getIndexedItems() {
		return index.getIndexedItems();
	}

	public void setNotifier(ProgressNotifier notifier) {
		this.notifier = notifier;
	}

	abstract protected Iterable<T> getItems() throws IOException;

	abstract protected void processItem(HaaSFileTransfer tr, T p) throws InterruptedIOException;

	abstract protected long getTotalSize(Iterable<T> items, HaaSFileTransfer tr) throws InterruptedIOException;

	private void doProcess(AtomicBoolean reRun) {
		runningProcessCounter.incrementAndGet();
		boolean interrupted = false;
		this.notifier.addItem(INIT_TRANSFER_ITEM);
		runningTransferThreads.add(Thread.currentThread());
		TransferFileProgressForHaaSClient actualnotifier = DUMMY_FILE_PROGRESS;
		try (HaaSFileTransfer tr = fileTransferSupplier.get()) {
			try {
				tr.setProgress(actualnotifier = getTransferFileProgress(tr));
			} catch (InterruptedIOException e1) {
				interrupted = true;
			}
			this.notifier.itemDone(INIT_TRANSFER_ITEM);
			this.notifier.done();
			do {
				synchronized (reRun) {
					if(interrupted || toProcessQueue.isEmpty()) {
						reRun.set(false);
						break;
					}
				}
				T p = toProcessQueue.poll();
				String item = p.toString();
				actualnotifier.addItem(item);
				try {
					processItem(tr, p);
					fileTransfered(p);
				} catch (InterruptedIOException e) {
					toProcessQueue.clear();
					interrupted = true;
				}
				actualnotifier.itemDone(item);
			} while(true);
		} finally {
			runningTransferThreads.remove(Thread.currentThread());
			synchronized (this) {
				if (startFinished) {
					if (!interrupted && !Thread.interrupted()) {
						processFinishedNotifier.run();
						actualnotifier.done();
					} else {
						notifyStop();
						reRun.set(false);
					}
				}
			}
			synchronized (runningProcessCounter) {
				runningProcessCounter.decrementAndGet();
				runningProcessCounter.notifyAll();
			}
		}
	}

	private void fileTransfered(T p) {
		try {
			index.remove(p);
			index.storeToWorkingFile();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	private TransferFileProgressForHaaSClient getTransferFileProgress(HaaSFileTransfer tr)
			throws InterruptedIOException {
		if (notifier == null) {
			return DUMMY_FILE_PROGRESS;
		}
		return new TransferFileProgressForHaaSClient(getTotalSize(toProcessQueue, tr), notifier);
	}

	private void notifyStop() {
		notifier.setCount(-1, -1);
	}

	private void waitForFinishAllProcesses() {
		synchronized (runningProcessCounter) {
			while (runningProcessCounter.get() != 0) {
				try {
					runningProcessCounter.wait();
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

}
