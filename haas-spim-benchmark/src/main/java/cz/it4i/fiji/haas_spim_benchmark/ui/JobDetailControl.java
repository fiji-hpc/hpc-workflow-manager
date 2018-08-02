
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import cz.it4i.fiji.haas_spim_benchmark.core.ObservableBenchmarkJob;
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

	private final ObservableBenchmarkJob job;

	public JobDetailControl(final ObservableBenchmarkJob job) {
		JavaFXRoutines.initRootAndController("JobDetail.fxml", this);
		observableValueRegistry = new HaaSOutputObservableValueRegistry(job
			.getValue(), Constants.HAAS_UPDATE_TIMEOUT /
				Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO);
		this.job = job;
	}

	// -- InitiableControl methods --

	@Override
	public void init(final Window parameter) {
		progressView.init(parameter);
		progressView.setJob(job);
		errorOutput.setObservable(observableValueRegistry.createObservable(
			SynchronizableFileType.StandardErrorFile));
		standardOutput.setObservable(observableValueRegistry.createObservable(
			SynchronizableFileType.StandardOutputFile));
		jobProperties.setJob(job);
		dataUpload.setJob(job);
		observableValueRegistry.start();
		if (job.getValue().getState() == JobState.Disposed) {
			// TODO: Handle this?
			if (log.isInfoEnabled()) {
				log.info("Job " + job.getValue().getId() +
						" state has been resolved as Disposed.");
			}
		}

		disableNonPermanentTabs();

		if (areExecutionDetailsAvailable()) {
			enableAllTabs();
		}
		
		setActiveFirstVisibleTab();
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
		return job.getValue().getState() == JobState.Running || job.getValue()
			.getState() == JobState.Finished || job.getValue()
				.getState() == JobState.Failed || job.getValue()
					.getState() == JobState.Canceled;
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

	private void setActiveFirstVisibleTab() {
		for(Tab t: getTabs()) {
			if(!t.isDisable()) {
				t.getTabPane().getSelectionModel().select(t);
				break;
			}
		}
	}
}
