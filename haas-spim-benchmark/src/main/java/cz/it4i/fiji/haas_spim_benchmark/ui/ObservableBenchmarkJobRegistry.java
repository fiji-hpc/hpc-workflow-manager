package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.nio.file.Files;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.ObservableValueRegistry;
import cz.it4i.fiji.haas.ui.UpdatableObservableValue.UpdateStatus;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.ObservableBenchmarkJob;
import javafx.beans.value.ObservableValue;

public class ObservableBenchmarkJobRegistry extends ObservableValueRegistry<BenchmarkJob,ObservableBenchmarkJob> {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.ObservableBenchmarkJobRegistry.class);

	private Executor executorUI;
	public ObservableBenchmarkJobRegistry(Consumer<BenchmarkJob> removeConsumer, Executor exec, Executor executorServiceFX) {
		super(t -> update(t,exec), t -> {
			return t.getStateAsync(exec).getNow(null);
		}, removeConsumer);
		executorUI = executorServiceFX;
	}
	
	@Override
	public ObservableBenchmarkJob addIfAbsent(BenchmarkJob value) {
		return (ObservableBenchmarkJob) super.addIfAbsent(value);
	}
	
	@Override
	public ObservableBenchmarkJob get(BenchmarkJob value) {
		return (ObservableBenchmarkJob) super.get(value);
	}
	
	@Override
	protected ObservableValue<BenchmarkJob> remove(BenchmarkJob value) {
		ObservableBenchmarkJob result = (ObservableBenchmarkJob) super.remove(value);
		result.removed();
		return result;
	}
	
	@Override
	protected ObservableBenchmarkJob constructObservableValue(BenchmarkJob v,
			Function<BenchmarkJob, UpdateStatus> updateFunction, Function<BenchmarkJob, Object> stateProvider) {
		return new ObservableBenchmarkJob(v, updateFunction, stateProvider, executorUI);
	}
	
	

	private static UpdateStatus update(BenchmarkJob t, Executor executor) {
		if (!Files.isDirectory(t.getDirectory())) {
			return UpdateStatus.Deleted;
		}
		JobState oldState = t.getStateAsync(executor).getNow(null);
		t.update();
		JobState newState = t.getStateAsync(executor).getNow(null);
		if (newState == null) {
			return UpdateStatus.Updated;
		}

		UpdateStatus result;
		result = oldState != newState ? UpdateStatus.Updated : UpdateStatus.NotUpdated;
		return result;
	}

	

}
