package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.nio.file.Files;
import java.util.function.Consumer;

import cz.it4i.fiji.haas.ui.ObservableValueRegistry;
import cz.it4i.fiji.haas.ui.UpdatableObservableValue.UpdateStatus;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;

public class ObservableBenchmarkJobRegistry extends ObservableValueRegistry<BenchmarkJob> {

	public ObservableBenchmarkJobRegistry(Consumer<BenchmarkJob> removeConsumer) {
		super(t -> update(t), t -> t.getState(), removeConsumer);
	}

	private static UpdateStatus update(BenchmarkJob t) {
		JobState oldState = t.getState();
		t.update();
		if (!Files.isDirectory(t.getDirectory())) {
			return UpdateStatus.Deleted;
		}
		UpdateStatus result = oldState != t.getState() ? UpdateStatus.Updated : UpdateStatus.NotUpdated;

		return result;
	}

}
