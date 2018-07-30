
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

public class JobDetailControl extends TabPane implements CloseableControl,
	InitiableControl
{

	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_spim_benchmark.ui.JobDetailControl.class);

	@FXML
	private SPIMPipelineProgressViewController progressView;

	@FXML
	private LogViewControl errorOutput;

	@FXML
	private LogViewControl standardOutput;

	@FXML
	private JobPropertiesControl jobProperties;

	@FXML
	private Tab jobPropertiesTab;

	@FXML
	private DataTransferController dataUpload;

	@FXML
	private Tab dataUploadTab;

	private final HaaSOutputObservableValueRegistry observableValueRegistry;

	private final BenchmarkJob job;

	public JobDetailControl(final BenchmarkJob job) {
		JavaFXRoutines.initRootAndController("JobDetail.fxml", this);
		progressView.setJob(job);
		observableValueRegistry = new HaaSOutputObservableValueRegistry(job,
			Constants.HAAS_UPDATE_TIMEOUT /
				Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO);
		errorOutput.setObservable(observableValueRegistry.createObservable(
			SynchronizableFileType.StandardErrorFile));
		standardOutput.setObservable(observableValueRegistry.createObservable(
			SynchronizableFileType.StandardOutputFile));
		jobProperties.setJob(job);
		dataUpload.setJob(job);
		observableValueRegistry.start();
		this.job = job;
	}

	// -- InitiableControl methods --

	@Override
	public void init(final Window parameter) {

		if (job.getState() == JobState.Disposed) {
			// TODO: Handle this?
			log.debug("Job " + job.getId() + " state has been resolved as Disposed.");
		}

		disableNonPermanentTabs();

		if (areExecutionDetailsAvailable()) {
			enableAllTabs();
		}
	}

	// -- CloseableControl methods --

	@Override
	public void close() {
		observableValueRegistry.close();

		// Close controllers
		progressView.close();
		jobProperties.close();
		dataUpload.close();
	}

	// -- Helper methods --

	/*
	 * Checks whether execution details are available
	 */
	private boolean areExecutionDetailsAvailable() {
		return job.getState() == JobState.Running || job
			.getState() == JobState.Finished || job.getState() == JobState.Failed ||
			job.getState() == JobState.Canceled;
	}

	/*
	 * Disables all tabs except those which shall be always enabled, such as job properties tab
	 */
	private void disableNonPermanentTabs() {
		getTabs().stream().filter(t -> t != jobPropertiesTab && t != dataUploadTab)
			.forEach(t -> t.setDisable(true));
		getSelectionModel().select(jobPropertiesTab);
	}

	/*
	 * Enables all tabs
	 */
	private void enableAllTabs() {
		getTabs().stream().forEach(t -> t.setDisable(false));
	}

}
