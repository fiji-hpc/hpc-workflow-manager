package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import cz.it4i.fiji.haas_spim_benchmark.core.TaskComputation;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;

public class TaskComputationAdapter implements Closeable {

	private final TaskComputation computation;

	private final List<ObservableOutputFile> outputs = new LinkedList<>();

	private final List<ObservableValue<String>> logs = new LinkedList<>();

	private final Timer timer;

	public TaskComputationAdapter(TaskComputation computation) {
		this.computation = computation;
		timer = new Timer();
		Map<String, Long> sizes = computation.getOutFileSizes();
		computation.getOutputs().forEach(outputFile -> addOutputFile(outputFile, sizes.get(outputFile)));
		timer.scheduleAtFixedRate(new P_TimerTask(), Constants.HAAS_TIMEOUT, Constants.HAAS_TIMEOUT);
	}

	private void addOutputFile(String outputFile, Long size) {
		outputs.add(new ObservableOutputFile(outputFile, size));
	}

	@Override
	public void close() {
		timer.cancel();
	}

	public static class Log {
		public String getName() {
			return null;
		}

		public ObservableValue<String> getContent() {
			return null;
		}
	}

	private class ObservableOutputFile extends ObservableValueBase<RemoteFileInfo> {

		private final String name;

		private Long size;

		private final RemoteFileInfo value = new RemoteFileInfo() {

			@Override
			public Long getSize() {
				return size;
			}

			@Override
			public String getName() {
				return name;
			}
		};

		public ObservableOutputFile(String name, Long size) {
			this.name = name;
			this.size = size;
		}

		@Override
		public RemoteFileInfo getValue() {
			return value;
		}

		public void setSize(Long newValue) {
			Long oldValue = size;
			size = newValue;
			if (oldValue != newValue && oldValue != null && !oldValue.equals(newValue)) {
				fireValueChangedEvent();
			}
		}
	}

	private class P_TimerTask extends TimerTask {

		@Override
		public void run() {
			Map<String, Long> sizes = computation.getOutFileSizes();
			outputs.forEach(value -> value.setSize(sizes.get(value.getValue().getName())));
		}

	}
}
