package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.util.function.Consumer;

import cz.it4i.fiji.haas.ui.ObservableValueRegistry;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_spim_benchmark.core.Task;
import cz.it4i.fiji.haas_spim_benchmark.core.TaskComputation;

public class ObservableTaskRegistry extends ObservableValueRegistry<Task>{

	public ObservableTaskRegistry(
			Consumer<Task> removeConsumer) {
		super(x->true, t->update(t), removeConsumer);
	}

	private static boolean update(Task t) {
		boolean result = true;
		t.update();
		for(TaskComputation tc: t.getComputations()) {
			JobState oldState = tc.getState();
			tc.update();
			result &= oldState == tc.getState();
		}
		return result;
	}

	

}
