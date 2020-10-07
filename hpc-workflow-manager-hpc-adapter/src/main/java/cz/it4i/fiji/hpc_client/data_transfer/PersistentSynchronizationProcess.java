
package cz.it4i.fiji.hpc_client.data_transfer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
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

import cz.it4i.fiji.commons.DoActionEventualy;
import cz.it4i.fiji.hpc_client.HPCClientException;
import cz.it4i.fiji.hpc_client.HPCFileTransfer;
import cz.it4i.fiji.hpc_client.Notifiers;
import cz.it4i.fiji.hpc_client.ProgressNotifier;

abstract class PersistentSynchronizationProcess<T> {

	private static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_client.data_transfer.PersistentSynchronizationProcess.class);

	private static final TransferFileProgressForHPCClient DUMMY_FILE_PROGRESS =
		new TransferFileProgressForHPCClient(0, Notifiers.emptyProgressNotifier());

	private static final String INIT_TRANSFER_ITEM = "init transfer";

	private static final long WAIT_FOR_CLOSE_SEESION_TIMEOUT = 500;

	private final PersistentIndex<T> index;

	private final Queue<T> toProcessQueue = new LinkedBlockingQueue<>();

	private final Set<Thread> runningTransferThreads = Collections
		.synchronizedSet(new HashSet<>());

	private final SimpleThreadRunner runner;

	private final Supplier<HPCFileTransfer> fileTransferSupplier;

	private final Runnable processFinishedNotifier;

	private ProgressNotifier notifier;

	private boolean startFinished = true;

	private final AtomicInteger runningProcessCounter = new AtomicInteger();

	private final Collection<HolderOfOpenClosables> openedClosables = Collections
		.synchronizedList(new LinkedList<>());

	public PersistentSynchronizationProcess(ExecutorService service,
		Supplier<HPCFileTransfer> fileTransferSupplier,
		Runnable processFinishedNotifier, Path indexFile,
		Function<String, T> convertor) throws IOException
	{
		runner = new SimpleThreadRunner(service);
		this.fileTransferSupplier = fileTransferSupplier;
		this.processFinishedNotifier = processFinishedNotifier;
		this.index = new PersistentIndex<>(indexFile, convertor);
	}

	public synchronized CompletableFuture<Void> start() throws IOException {
		startFinished = false;
		index.clear();
		try {
			for (T item : getItems()) {
				index.insert(item);
				toProcessQueue.add(item);
			}
			index.storeToWorkingFile();
			return runner.runIfNotRunning(this::doProcess);
		}
		finally {
			startFinished = true;
			index.storeToWorkingFile();
		}
	}

	public void stop() throws IOException {
		toProcessQueue.clear();
		index.clear();
		notifyStop();
		runningTransferThreads.forEach(Thread::interrupt);
	}

	public void shutdown() {
		synchronized (this) {
			toProcessQueue.clear();
			runningTransferThreads.forEach(Thread::interrupt);
		}
		try (DoActionEventualy action = new DoActionEventualy(
			WAIT_FOR_CLOSE_SEESION_TIMEOUT, this::closeOpennedClosables))
		{
			waitForFinishAllProcesses();
		}
	}

	public void resume() {
		synchronized (this) {
			toProcessQueue.addAll(index.getIndexedItems());
		}
		runner.runIfNotRunning(this::doProcess);
	}

	public Set<T> getIndexedItems() {
		return index.getIndexedItems();
	}

	public void setNotifier(ProgressNotifier notifier) {
		this.notifier = notifier;
	}

	public boolean isWorking() {
		return !toProcessQueue.isEmpty();
	}

	protected abstract Iterable<T> getItems() throws IOException;

	protected abstract void processItem(HPCFileTransfer tr, T p)
		throws InterruptedIOException;

	protected abstract long getTotalSize(Iterable<T> items, HPCFileTransfer tr)
		throws InterruptedIOException;

	private void doProcess(AtomicBoolean hasAlreadyRun) {
		runningProcessCounter.incrementAndGet();
		boolean interrupted = false;
		this.notifier.addItem(INIT_TRANSFER_ITEM);
		runningTransferThreads.add(Thread.currentThread());
		TransferFileProgressForHPCClient actualNotifier = DUMMY_FILE_PROGRESS;
		try (HolderOfOpenClosables transferHolder = new HolderOfOpenClosables(
			fileTransferSupplier.get()))
		{
			HPCFileTransfer tr = transferHolder.getTransfer();
			try {
				actualNotifier = getTransferFileProgress(tr);
				tr.setProgress(actualNotifier);
			}
			catch (InterruptedIOException e1) {
				interrupted = true;
			}
			this.notifier.itemDone(INIT_TRANSFER_ITEM);
			this.notifier.done();
			do {
				String item;
				T p;
				synchronized (this) {
					synchronized (hasAlreadyRun) {
						interrupted |= Thread.interrupted();
						if (interrupted || toProcessQueue.isEmpty()) {
							hasAlreadyRun.set(false);
							break;
						}
					}
					p = toProcessQueue.poll();
					item = p.toString();
				}
				actualNotifier.addItem(item);
				try {
					processItem(tr, p);
					fileTransfered(p);
					actualNotifier.itemDone(item);
				}
				catch (InterruptedIOException | HPCClientException e) {
					synchronized (this) {
						toProcessQueue.clear();
						interrupted = true;
						if (e instanceof HPCClientException) {
							log.warn("process ", e);
							actualNotifier.addItem(Synchronization.FAILED_ITEM);
						}
						else {
							Thread.currentThread().interrupt();
						}
					}
				}

			}
			while (true);
		}
		finally {
			runningTransferThreads.remove(Thread.currentThread());
			synchronized (this) {
				if (startFinished) {
					if (!interrupted && !Thread.interrupted()) {
						processFinishedNotifier.run();
						actualNotifier.done();
					}
					else {
						notifyStop();
						hasAlreadyRun.set(false);
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
		}
		catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	private TransferFileProgressForHPCClient getTransferFileProgress(
		HPCFileTransfer tr) throws InterruptedIOException
	{
		if (notifier == null) {
			return DUMMY_FILE_PROGRESS;
		}
		return new TransferFileProgressForHPCClient(getTotalSize(toProcessQueue,
			tr), notifier);
	}

	private void notifyStop() {
		notifier.setCount(-1, -1);
	}

	private void waitForFinishAllProcesses() {
		synchronized (runningProcessCounter) {
			while (runningProcessCounter.get() != 0) {
				try {
					runningProcessCounter.wait();
				}
				catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	protected void closeOpennedClosables() {
		synchronized (openedClosables) {
			openedClosables.forEach(HolderOfOpenClosables::close);
		}
	}

	private class HolderOfOpenClosables implements Closeable {

		private final HPCFileTransfer transfer;

		public HolderOfOpenClosables(HPCFileTransfer transfer) {
			this.transfer = transfer;
			openedClosables.add(this);
		}

		public HPCFileTransfer getTransfer() {
			return this.transfer;
		}

		@Override
		public void close() {
			openedClosables.remove(this);
		}
	}
}
