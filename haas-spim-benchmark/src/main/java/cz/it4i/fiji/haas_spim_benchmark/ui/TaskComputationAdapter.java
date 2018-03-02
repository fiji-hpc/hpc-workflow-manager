package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.it4i.fiji.haas_spim_benchmark.core.TaskComputation;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;

public class TaskComputationAdapter implements Closeable{

	private final TaskComputation computation;
	
	private final List<ObservableValue<RemoteFileInfo>> outputs = new LinkedList<>();
	
	private final List<ObservableValue<String>> logs = new LinkedList<>();
	
	private final Timer timer;
	

	public TaskComputationAdapter(TaskComputation computation) {
		this.computation = computation;
		timer = new Timer();
		computation.getOutputs().forEach(outputFile->addOutputFile(outputFile));
	}



	private void addOutputFile(String outputFile) {
		outputs.add(new ObservableOutputFile(outputFile));
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
		
		private final RemoteFileInfo value = new RemoteFileInfo() {
			
			@Override
			public long getSize() {
				return 0;
			}
			
			@Override
			public String getName() {
				return null;
			}
		};
		
		public ObservableOutputFile(String name) {
			this.name = name;
		}

		@Override
		public RemoteFileInfo getValue() {
			return value;
		}
	}
	
	private class P_TimerTask extends TimerTask {

		@Override
		public void run() {
			
		}
		
	}
}
