
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.haas_spim_benchmark.core.ObservableBenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.SimpleObservableValue;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
	private LogViewControl snakemakeOutputControl;

	@FXML
	private Tab snakemakeOutputTab;

	@FXML
	private LogViewControl otherOutputControl;

	@FXML
	private Tab otherOutputTab;

	@FXML
	private JobPropertiesControl jobProperties;

	@FXML
	private Tab jobPropertiesTab;

	@FXML
	private DataTransferController dataUpload;

	@FXML
	private Tab dataUploadTab;

	private final ObservableBenchmarkJob job;

	private SimpleObservableValue<String> errorOutput;

	private final ChangeListener<String> errorOutputListener =
		new ChangeListener<String>()
		{

			@Override
			public void changed(ObservableValue<? extends String> observable,
				String oldValue, String newValue)
		{
				snakemakeOutputTab.setDisable(newValue.isEmpty());
			}

		};

	private SimpleObservableValue<String> standardOutput;

	private final ChangeListener<String> standardOutputListener =
		new ChangeListener<String>()
		{

			@Override
			public void changed(ObservableValue<? extends String> observable,
				String oldValue, String newValue)
		{
				otherOutputTab.setDisable(newValue.isEmpty());
			}

		};

	public JobDetailControl(final ObservableBenchmarkJob job) {
		JavaFXRoutines.initRootAndController("JobDetail.fxml", this);
		this.job = job;
	}

	// -- InitiableControl methods --

	@Override
	public void init(final Window parameter) {

		progressView.init(parameter);
		progressView.setJob(job);

		errorOutput = job.getObservableSnakemakeOutput(
			SynchronizableFileType.StandardErrorFile);
		errorOutput.addListener(errorOutputListener);
		snakemakeOutputControl.setObservable(errorOutput);

		standardOutput = job.getObservableSnakemakeOutput(
			SynchronizableFileType.StandardOutputFile);
		standardOutput.addListener(standardOutputListener);
		otherOutputControl.setObservable(standardOutput);

		jobProperties.setJob(job);
		dataUpload.setJob(job);

		if (job.getValue().getState() == JobState.Disposed) {
			// TODO: Handle this?
			if (log.isInfoEnabled()) {
				log.info("Job " + job.getValue().getId() +
					" state has been resolved as Disposed.");
			}
		}

		if (areExecutionDetailsAvailable()) {
			enableAllTabs();
		}
		else {
			disableNonPermanentTabs();
		}

		setActiveFirstVisibleTab();
	}

	// -- CloseableControl methods --

	@Override
	public void close() {

		// Close controllers
		progressView.close();
		errorOutput.removeListener(errorOutputListener);
		snakemakeOutputControl.close();
		standardOutput.removeListener(standardOutputListener);
		otherOutputControl.close();
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
	}

	/*
	 * Enables all tabs
	 */
	private void enableAllTabs() {
		getTabs().stream().forEach(t -> t.setDisable(false));
	}

	private void setActiveFirstVisibleTab() {
		for (final Tab t : getTabs()) {
			if (!t.isDisable()) {
				t.getTabPane().getSelectionModel().select(t);
				break;
			}
		}
	}
}
