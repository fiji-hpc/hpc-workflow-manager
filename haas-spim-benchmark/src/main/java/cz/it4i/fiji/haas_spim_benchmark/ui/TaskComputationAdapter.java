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

public class TaskComputationAdapter implements Closeable {

	public final static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.TaskComputationAdapter.class);

	private final TaskComputation computation;

	private final List<ObservableValue<RemoteFileInfo>> outputs = new LinkedList<>();

	private final List<ObservableLog> logs = new LinkedList<>();

	private Timer timer;

	public TaskComputationAdapter(TaskComputation computation) {
		this.computation = computation;
		timer = new Timer();
	}

	public void init() {
		Map<String, Long> sizes = computation.getOutFileSizes();
		computation.getOutputs().forEach(outputFile -> addOutputFile(outputFile, sizes.get(outputFile)));
		computation.getLogs().forEach(decoratedLog -> logs.add(new ObservableLog(decoratedLog)));
		synchronized (this) {
			if(timer != null) {
				timer.schedule(new P_TimerTask(), Constants.HAAS_TIMEOUT, Constants.HAAS_TIMEOUT);
			}
		}
		
	}

	@Override
	public synchronized void close() {
		timer.cancel();
		timer = null;
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

	public static class ObservableLog  {
	
		private final String name;
		
		private  final P_ObservableString value;
	
		public ObservableLog(Log content) {
			this.value = new P_ObservableString(content.getContent());
			this.name = content.getName();
		}
	
		public String getName() {
			return name;
		}
		
		public ObservableValue<String> getContent() {
			return value;
		}
	
		public void setContentValue(Log log) {
			if (!getName().equals(log.getName())) {
				throw new IllegalArgumentException(
						"this.name=" + getName() + ", log.name=" + log.getName());
			}
			value.setValue(log.getContent());
		}
	
		private class P_ObservableString extends ObservableValueBase<String> {
	
			private String innerValue;
	
			public P_ObservableString(String value) {
				this.innerValue = value;
			}
	
			@Override
			public String getValue() {
				return innerValue;
			}
			
			public void setValue(String value) {
				if(this.innerValue != null && !this.innerValue.equals(value) ||
					value != null && !value.equals(this.innerValue)) {
					this.innerValue = value;
					fireValueChangedEvent();
				}
			}
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
			Map<String, Log> computationLogs = computation.getLogs().stream()
					.collect(Collectors.<Log, String, Log>toMap((Log l) -> l.getName(), (Log l) -> l));
			TaskComputationAdapter.this.logs
					.forEach(processedLog -> processedLog.setContentValue(computationLogs.get(processedLog.getName())));
			outputs.forEach(value -> ((ObservableOutputFile) value).setSize(sizes.get(value.getValue().getName())));
		}

	}
}
