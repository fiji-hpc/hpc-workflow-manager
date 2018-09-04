
package cz.it4i.fiji.haas_spim_benchmark.core;

import com.google.common.collect.Streams;

import java.io.Closeable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;

class HaasOutputObservableValueRegistry implements Closeable {

	private final BenchmarkJob job;
	private final Map<SynchronizableFileType, SimpleObservableValue<String>> observableValues =
		new HashMap<>();
	private Timer timer;
	private boolean isRunning = false;
	private int numberOfListeners = 0;

	public HaasOutputObservableValueRegistry(final BenchmarkJob job) {
		this.job = job;
		this.observableValues.put(SynchronizableFileType.StandardOutputFile,
			createObservableValue());
		this.observableValues.put(SynchronizableFileType.StandardErrorFile,
			createObservableValue());
	}

	@Override
	public synchronized void close() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}

	public SimpleObservableValue<String> getObservableOutput(
		final SynchronizableFileType type)
	{
		return observableValues.get(type);
	}

	private SimpleObservableValue<String> createObservableValue() {
		return new SimpleObservableValue<>(null, this::increaseNumberOfObservers,
			this::decreaseNumberOfObservers);
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

					final List<SynchronizableFileType> types = new LinkedList<>(
						observableValues.keySet());

					Streams.zip(types.stream(), job.getComputationOutput(types).stream(),
						(type, value) -> (Runnable) (() -> observableValues.get(type)
							.update(value))).forEach(r -> r.run());
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
