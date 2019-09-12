package cz.it4i.fiji.hpc_workflow.ui;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.ObservableValueRegistry;
import cz.it4i.fiji.haas.ui.UpdatableObservableValue.UpdateStatus;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.hpc_workflow.core.ObservableHPCWorkflowJob;
import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowJobManager.BenchmarkJob;

public class ObservableHPCWorkflowJobRegistry extends ObservableValueRegistry<BenchmarkJob,ObservableHPCWorkflowJob> {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.hpc_workflow.ui.ObservableHPCWorkflowJobRegistry.class);

	private Executor executorUI;
	public ObservableHPCWorkflowJobRegistry(Consumer<BenchmarkJob> removeConsumer, Executor exec, Executor executorServiceFX) {
		super(t -> update(t, exec), t -> t.getStateAsync(exec).getNow(null),
			removeConsumer);
		executorUI = executorServiceFX;
	}

	@Override
	public void close() {
		super.getAllItems().forEach(ObservableHPCWorkflowJob::close);
	}

	@Override
	protected ObservableHPCWorkflowJob remove(BenchmarkJob value) {
		ObservableHPCWorkflowJob result = super.remove(value);
		result.removed();
		return result;
	}
	
	@Override
	protected ObservableHPCWorkflowJob constructObservableValue(BenchmarkJob benchmarkJob) {
		return new ObservableHPCWorkflowJob(benchmarkJob, getUpdateFunction(), getStateProvider(), executorUI);
	}	
	

	private static UpdateStatus update(BenchmarkJob t, Executor executor) {
		if (!t.getDirectory().toFile().isDirectory()) {
			return UpdateStatus.DELETED;
		}
		JobState oldState = t.getStateAsync(executor).getNow(null);
		t.update();
		JobState newState = t.getStateAsync(executor).getNow(null);
		if (newState == null) {
			return UpdateStatus.UPDATED;
		}

		UpdateStatus result;
		result = oldState != newState ? UpdateStatus.UPDATED : UpdateStatus.NOT_UPDATED;
		return result;
	}

	

}
