package cz.it4i.fiji.haas.data_transfer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SimpleThreadRunner {
	private final ExecutorService service;
	private final AtomicBoolean reRun = new AtomicBoolean(false);
	private CompletableFuture<?> lastRun;

	public SimpleThreadRunner(ExecutorService service) {
		this.service = service;
	}

	synchronized public CompletableFuture<?> runIfNotRunning(Consumer<AtomicBoolean> r) {
		synchronized (reRun) {
			if (reRun.get()) {
				return lastRun;
			}
			reRun.set(true);
		}
		return lastRun = CompletableFuture.runAsync(() -> {
			do {
				r.accept(reRun);
			} while (reRun.get());
		}, service);
	}
}
