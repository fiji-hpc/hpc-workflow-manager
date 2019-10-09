
package cz.it4i.fiji.hpc_workflow.core;

import static cz.it4i.fiji.hpc_workflow.core.Configuration.getHaasUpdateTimeout;
import static cz.it4i.fiji.hpc_workflow.core.Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.it4i.fiji.hpc_workflow.Task;
import cz.it4i.fiji.hpc_workflow.WorkflowJob;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;

class TaskObservableValueRegistry implements Closeable {

	private static final Task EMPTY_TASK = new TaskImpl(
		new ComputationAccessorAdapter(), "", 0);

	private final WorkflowJob job;
	private final SimpleObservableList<Task> observableTaskList;
	private Timer timer;
	private boolean isRunning = false;
	private boolean closed = false;

	public TaskObservableValueRegistry(final WorkflowJob job) {
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

		class LTimerTask extends TimerTask {

			@Override
			public void run() {
				List<Task> tasks = job.getTasks();
				JavaFXRoutines.runOnFxThread(() -> observableTaskList.setAll(tasks));

				synchronized (TaskObservableValueRegistry.this) {
					if (timer != null) {
						timer.schedule(new LTimerTask(), getHaasUpdateTimeout() /
							UI_TO_HAAS_FREQUENCY_UPDATE_RATIO);
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
			timer.schedule(new LTimerTask(), 0);
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
