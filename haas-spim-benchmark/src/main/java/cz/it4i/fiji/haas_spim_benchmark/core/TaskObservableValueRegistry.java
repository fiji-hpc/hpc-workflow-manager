
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;

class TaskObservableValueRegistry implements Closeable {

	private final static Task EMPTY_TASK = new Task(
		new SPIMComputationAccessorAdapter(), "", 0);

	private final BenchmarkJob job;
	private final SimpleObservableList<Task> observableTaskList;
	private Timer timer;
	private boolean isRunning = false;
	private boolean closed = false;

	public TaskObservableValueRegistry(final BenchmarkJob job) {
		this.job = job;
		this.observableTaskList = new SimpleObservableList<>(new ArrayList<Task>(),
			this::evaluateTimer);
		this.observableTaskList.add(EMPTY_TASK);
	}

	@Override
	public synchronized void close() {
		stopTimer();
		closed = true;
	}

	public SimpleObservableList<Task> getTaskList() {
		return observableTaskList;
	}

	private synchronized void evaluateTimer() {

		class L_TimerTask extends TimerTask {

			@Override
			public void run() {
				List<Task> tasks = job.getTasks();
				JavaFXRoutines.runOnFxThread(() -> observableTaskList.setAll(tasks));

				synchronized (TaskObservableValueRegistry.this) {
					if (timer != null) {
						timer.schedule(new L_TimerTask(), Constants.HAAS_UPDATE_TIMEOUT /
							Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO);
					}
				}
			}
		}

		if (closed) {
			return;
		}

		final boolean anyListeners = observableTaskList.hasAnyListeners();

		if (!isRunning && anyListeners) {
			timer = new Timer();
			timer.schedule(new L_TimerTask(), 0);
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
