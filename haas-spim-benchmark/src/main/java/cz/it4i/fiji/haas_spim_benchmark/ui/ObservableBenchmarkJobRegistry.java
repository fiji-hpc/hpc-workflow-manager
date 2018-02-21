package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.nio.file.Files;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.ObservableValueRegistry;
import cz.it4i.fiji.haas.ui.UpdatableObservableValue.UpdateStatus;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;

public class ObservableBenchmarkJobRegistry extends ObservableValueRegistry<BenchmarkJob> {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.ObservableBenchmarkJobRegistry.class);

	public ObservableBenchmarkJobRegistry(Consumer<BenchmarkJob> removeConsumer, Executor exec) {
		super(t -> update(t,exec), t -> {
			return t.getStateAsync(exec).getNow(null);
		}, removeConsumer);
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
