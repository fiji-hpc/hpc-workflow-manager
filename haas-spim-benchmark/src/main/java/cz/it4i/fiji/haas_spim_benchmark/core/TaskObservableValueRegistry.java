
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.io.Closeable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import javafx.beans.value.ObservableValue;

class TaskObservableValueRegistry implements Closeable {

	private final BenchmarkJob job;
	private final SimpleObservableValue<List<Task>> observableTaskList;
	private Timer timer;
	private boolean isRunning = false;
	private int numberOfListeners = 0;

	public TaskObservableValueRegistry(final BenchmarkJob job) {
		this.job = job;
		this.observableTaskList = new SimpleObservableValue<>(null,
			this::increaseNumberOfObservers, this::decreaseNumberOfObservers);
	}

	@Override
	public void close() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}

	public ObservableValue<List<Task>> getTaskList() {
		return observableTaskList;
	}

	private synchronized void increaseNumberOfObservers() {
		numberOfListeners++;
		evaluateTimer();
	}

	private synchronized void decreaseNumberOfObservers() {
		numberOfListeners--;
		evaluateTimer();
	}

	private void evaluateTimer() {

		final boolean anyListeners = numberOfListeners > 0;

		if (!isRunning && anyListeners) {
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					observableTaskList.update(job.getTasks());
				}
			}, 0, Constants.HAAS_UPDATE_TIMEOUT /
				Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO);
			isRunning = true;
		}
		else if (isRunning && !anyListeners) {
			timer.cancel();
			timer.purge();
			isRunning = false;
		}

	}

}
