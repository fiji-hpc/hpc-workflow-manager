package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import cz.it4i.fiji.haas_spim_benchmark.core.TaskComputation;
import cz.it4i.fiji.haas_spim_benchmark.core.TaskComputation.Log;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;

//TASK: Pokračovat zapojením do UI - upravit pro TaskComputation
//TASK: dodělat progress dialog + modalita
//            
public class TaskComputationAdapter implements Closeable {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.TaskComputationAdapter.class);

	private final TaskComputation computation;

	private final List<ObservableValue<RemoteFileInfo>> outputs = new LinkedList<>();

	private final List<ObservableLog> logs = new LinkedList<>();

	private final Timer timer;

	public TaskComputationAdapter(TaskComputation computation) {
		this.computation = computation;
		timer = new Timer();
		Map<String, Long> sizes = computation.getOutFileSizes();
		computation.getOutputs().forEach(outputFile -> addOutputFile(outputFile, sizes.get(outputFile)));
		computation.getLogs().forEach(log->logs.add(new ObservableLog(log)));
		timer.scheduleAtFixedRate(new P_TimerTask(), Constants.HAAS_TIMEOUT, Constants.HAAS_TIMEOUT);
	}

	@Override
	public void close() {
		timer.cancel();
	}

	public List<ObservableValue<RemoteFileInfo>> getOutputs() {
		return outputs;
	}
	
	public List<ObservableLog> getLogs() {
		return logs;
	}

	private void addOutputFile(String outputFile, Long size) {
		outputs.add(new ObservableOutputFile(outputFile, size));
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

	public static class ObservableLog  {

		private final String name;
		
		private  final P_ObservableString value;

		public ObservableLog(Log content) {
			this.value = new P_ObservableString(content.getContent());
			this.name = log.getName();
		}

		public String getName() {
			return name;
		}
		
		public ObservableValue<String> getContent() {
			return value;
		}

		public void setContentValue(Log log) {
			if (!log.getName().equals(log.getName())) {
				throw new IllegalArgumentException(
						"this.name=" + getName() + ", log.name=" + log.getName());
			}
			value.setValue(log.getContent());
		}

		private class P_ObservableString extends ObservableValueBase<String> {

			private String value;

			public P_ObservableString(String value) {
				this.value = value;
			}

			@Override
			public String getValue() {
				return value;
			}
			
			public void setValue(String value) {
				if(this.value != null && !this.value.equals(value) ||
					value != null && !value.equals(this.value)) {
					this.value = value;
					fireValueChangedEvent();
				}
			}
		}
		
	}

	private class P_TimerTask extends TimerTask {

		@Override
		public void run() {
			Map<String, Long> sizes = computation.getOutFileSizes();
			Map<String, Log> logs = computation.getLogs().stream()
					.collect(Collectors.<Log, String, Log>toMap((Log log) -> log.getName(), (Log log) -> log));
			TaskComputationAdapter.this.logs.forEach(log->((ObservableLog) log).setContentValue(logs.get(log.getName())));
			outputs.forEach(value -> ((ObservableOutputFile) value).setSize(sizes.get(value.getValue().getName())));
		}

	}
}
