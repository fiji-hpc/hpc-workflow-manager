package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class JobDetailControl extends TabPane implements CloseableControl, InitiableControl {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.JobDetailControl.class);

	@FXML private SPIMPipelineProgressViewController progressView;

	@FXML private LogViewControl errorOutput;

	@FXML private LogViewControl standardOutput;
	
	@FXML private JobPropertiesControl jobProperties;
	
	@FXML private Tab jobPropertiesTab;
	
	private final HaaSOutputObservableValueRegistry observableValueRegistry;

	private final BenchmarkJob job;

	
	public JobDetailControl(BenchmarkJob job) {
		JavaFXRoutines.initRootAndController("JobDetail.fxml", this);
		progressView.setJob(job);
		observableValueRegistry = new HaaSOutputObservableValueRegistry(job,
				Constants.HAAS_UPDATE_TIMEOUT / Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO);
		errorOutput.setObservable(observableValueRegistry.createObservable(SynchronizableFileType.StandardErrorFile));
		standardOutput
				.setObservable(observableValueRegistry.createObservable(SynchronizableFileType.StandardOutputFile));
		jobProperties.setJob(job);
		observableValueRegistry.start();
		this.job = job;
	}

	@Override
	public void init(Window parameter) {
		if (!isExecutionDetailsAvailable(job)) {
			enableOnlySpecificTab(jobPropertiesTab);
		}
	}
	
	
	private void enableOnlySpecificTab(Tab tabToLeaveEnabled) {
		getTabs().stream().filter(node -> node != jobPropertiesTab).forEach(node -> node.setDisable(true));
		getSelectionModel().select(jobPropertiesTab);
	}

	private boolean isExecutionDetailsAvailable(BenchmarkJob job) {
		return job.getState() == JobState.Running || job.getState() == JobState.Finished
				|| job.getState() == JobState.Failed || job.getState() == JobState.Canceled;
	}

	@Override
	public void close() {
		observableValueRegistry.close();
		progressView.close();
		jobProperties.close();
	}
}
