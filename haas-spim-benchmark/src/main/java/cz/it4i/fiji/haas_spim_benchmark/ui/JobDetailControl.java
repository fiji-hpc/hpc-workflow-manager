package cz.it4i.fiji.haas_spim_benchmark.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;

public class JobDetailControl extends TabPane implements CloseableControl {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.JobDetailControl.class);

	@FXML
	private SPIMPipelineProgressViewController progressView;

	@FXML
	private LogViewControl errorOutput;

	@FXML
	private LogViewControl standardOutput;

	private HaaSOutputObservableValueRegistry observableValueRegistry;

	public JobDetailControl(BenchmarkJob job) {
		JavaFXRoutines.initRootAndController("JobDetail.fxml", this);
		progressView.setJob(job);
		observableValueRegistry = new HaaSOutputObservableValueRegistry(job,
				Constants.HAAS_UPDATE_TIMEOUT / Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO);
		errorOutput.setObservable(observableValueRegistry.createObservable(SynchronizableFileType.StandardErrorFile));
		standardOutput.setObservable(observableValueRegistry.createObservable(SynchronizableFileType.StandardOutputFile));
		observableValueRegistry.start();
	}

	@Override
	public void close() {
		observableValueRegistry.close();
		progressView.close();
	}
}
