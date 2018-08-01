package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import cz.it4i.fiji.haas.ui.ObservableValueRegistry;
import cz.it4i.fiji.haas.ui.UpdatableObservableValue;
import cz.it4i.fiji.haas.ui.UpdatableObservableValue.UpdateStatus;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_spim_benchmark.core.ObservableBenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.Task;
import cz.it4i.fiji.haas_spim_benchmark.core.TaskComputation;

public class ObservableTaskRegistry extends
	ObservableValueRegistry<Task, UpdatableObservableValue<Task>>
{

	private final Supplier<ObservableBenchmarkJob> jobSupplier;

	public ObservableTaskRegistry(Supplier<ObservableBenchmarkJob> jobSupplier,
		Consumer<Task> removeConsumer)
	{
		super(t -> update(t), t -> t.getComputations().stream().map(tc -> tc
			.getState()).collect(Collectors.toList()), removeConsumer);
		this.jobSupplier = jobSupplier;
	}

	@Override
	public void update() {
		jobSupplier.get().getValue().getTasks();
		super.update();
	}
	
	@Override
	protected UpdatableObservableValue<Task> constructObservableValue(Task task) {
		return new UpdatableObservableValue<>(task, getUpdateFunction(), getStateProvider());
	}

	private static UpdateStatus update(Task t) {
		boolean updated = false;
		t.update();
		for (TaskComputation tc : t.getComputations()) {
			JobState oldState = tc.getState();
			tc.update();
			updated |= oldState != tc.getState();
		}
		return updated ? UpdateStatus.Updated : UpdateStatus.NotUpdated;
	}

}
