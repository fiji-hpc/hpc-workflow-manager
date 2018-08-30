
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

	public TaskObservableValueRegistry(final BenchmarkJob job) {
		this.job = job;
		this.observableTaskList = new SimpleObservableList<>(new ArrayList<Task>(),
			this::evaluateTimer);
	}

	@Override
	public void close() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}

	public SimpleObservableList<Task> getTaskList() {
		return observableTaskList;
	}

	private void evaluateTimer() {

		final boolean anyListeners = observableTaskList.hasAnyListeners();

		if (!isRunning && anyListeners) {
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					observableTaskList.setAll(job.getTasks());
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
