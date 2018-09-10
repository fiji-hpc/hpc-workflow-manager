
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;

class TaskObservableValueRegistry implements Closeable {

	private final BenchmarkJob job;
	private final SimpleObservableList<Task> observableTaskList;
	private Timer timer;
	private boolean isRunning = false;
	private boolean closed = false;
	private boolean waitForFirstTimerRun = false;
	private Thread waitingThread;

	public TaskObservableValueRegistry(final BenchmarkJob job) {
		this.job = job;
		this.observableTaskList = new SimpleObservableList<>(new ArrayList<Task>(),
			this::evaluateTimer);
	}

	//TODO close neverCalled
	@Override
	public synchronized void close() {
		if (waitingThread != null) {
			waitingThread.interrupt();
		}
		stopTimer();
		closed = true;
	}

	public SimpleObservableList<Task> getTaskList() {
		return observableTaskList;
	}

	private synchronized void evaluateTimer() {

		if (closed) {
			return;
		}

		final boolean anyListeners = observableTaskList.hasAnyListeners();

		if (!isRunning && anyListeners) {

			timer = new Timer();
			waitForFirstTimerRun = true;
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					observableTaskList.setAll(job.getTasks());
					synchronized(TaskObservableValueRegistry.this) {
						waitForFirstTimerRun = false;
						TaskObservableValueRegistry.this.notifyAll();
					}
				}
			}, 0, Constants.HAAS_UPDATE_TIMEOUT /
				Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO);
			while (waitForFirstTimerRun) {
				try {
					this.waitingThread = Thread.currentThread();
					this.wait();
					this.waitingThread = null;
				}
				catch (InterruptedException exc) {
					//ignore and return
					return;
				}
			}
			isRunning = true;
		}
		else if (isRunning && !anyListeners) {
			stopTimer();
		}
	}

	private void stopTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
			isRunning = false;
		}
	}

}
