
package cz.it4i.fiji.hpc_client.data_transfer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

class SimpleThreadRunner {

	private final ExecutorService service;
	private final AtomicBoolean hasAlreadyRun = new AtomicBoolean(false);
	private CompletableFuture<Void> promisedRun;

	public SimpleThreadRunner(ExecutorService service) {
		this.service = service;
	}

	public synchronized CompletableFuture<Void> runIfNotRunning(
		Consumer<AtomicBoolean> process)
	{
		synchronized (hasAlreadyRun) {
			if (hasAlreadyRun.get()) {
				return promisedRun;
			}
			hasAlreadyRun.set(true);
		}
		promisedRun = CompletableFuture.runAsync(() -> {
			do {
				// Usually calls doProcess() of PersistentSynchronizationProcess.
				process.accept(hasAlreadyRun);
			}
			while (hasAlreadyRun.get());
		}, service);
		return promisedRun;
	}
}
