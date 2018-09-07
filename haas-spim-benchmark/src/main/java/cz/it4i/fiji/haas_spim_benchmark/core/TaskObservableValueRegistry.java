
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;

class TaskObservableValueRegistry implements Closeable {

	private final BenchmarkJob job;
	private final SimpleObservableList<Task> observableTaskList;
	private Timer timer;
	private boolean isRunning = false;
	private boolean closed = false;

	public TaskObservableValueRegistry(final BenchmarkJob job) {
		this.job = job;
		this.observableTaskList = new SimpleObservableList<>(new ArrayList<Task>(),
			this::evaluateTimer);
	}

	@Override
	public synchronized void close() {
		stopTimer();
		closed = true;
	}

	public synchronized SimpleObservableList<Task> getTaskList() {
		return observableTaskList;
	}

	private void evaluateTimer() {

		if (closed) {
			return;
		}

		final boolean anyListeners = observableTaskList.hasAnyListeners();
		final CountDownLatch timerLatch = new CountDownLatch(1);

		if (!isRunning && anyListeners) {

			timer = new Timer();
			synchronized (timer) {
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						observableTaskList.setAll(job.getTasks());
						timerLatch.countDown();
					}
				}, 0, Constants.HAAS_UPDATE_TIMEOUT /
					Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO);

				try {
					timerLatch.await(10, TimeUnit.SECONDS);
				}
				catch (InterruptedException exc) {
					// TODO Handle properly
				}
				isRunning = true;
			}
		}
		else if (isRunning && !anyListeners) {
			synchronized (timer) {
				stopTimer();
			}
		}
	}

	private void stopTimer() {
		if (timer != null) {
			timer.cancel();
			isRunning = false;
		}
	}

}
