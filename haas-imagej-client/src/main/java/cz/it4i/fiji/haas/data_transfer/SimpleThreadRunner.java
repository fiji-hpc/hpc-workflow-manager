package cz.it4i.fiji.haas.data_transfer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SimpleThreadRunner {
	private ExecutorService service;
	private AtomicBoolean reRun = new AtomicBoolean(false);

	public SimpleThreadRunner(ExecutorService service) {
		this.service = service;
	}

	public void runIfNotRunning(Consumer<AtomicBoolean> r) {
		synchronized (this) {
			if (reRun.get()) {
				return;
			}
			reRun.set(true);
		}
		service.execute(() -> {
			do {
				r.accept(reRun);
			} while (reRun.get());
		});
	}
}
