
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
	private SPIMPipelineProgressViewController progressControl;

	@FXML
	private Tab progressTab;

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
				if (newValue != null) {

					snakemakeOutputTab.setDisable(newValue.isEmpty());
					// Although we're bringing some bits of business logic here
					// (the fact that task info is parsed from Snakemake output),
					// it is way easier than adding a dedicated ListChangeListener.
					progressTab.setDisable(newValue.isEmpty());

					setActiveFirstVisibleTab(false);
				}
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
				if (newValue != null) {
					otherOutputTab.setDisable(newValue.isEmpty());

					setActiveFirstVisibleTab(false);
				}
			}

		};

	public JobDetailControl(final ObservableBenchmarkJob job) {
		JavaFXRoutines.initRootAndController("JobDetail.fxml", this);
		this.job = job;
	}

	// -- InitiableControl methods --

	@Override
	public void init(final Window parameter) {

		progressControl.init(parameter);
		progressControl.setObservable(job.getObservableTaskList());

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

		errorOutputListener.changed(null, null, errorOutput.getValue());
		standardOutputListener.changed(null, null, standardOutput.getValue());

		setActiveFirstVisibleTab(true);

	}

	// -- CloseableControl methods --

	@Override
	public void close() {

		// Close controllers
		progressControl.close();
		errorOutput.removeListener(errorOutputListener);
		snakemakeOutputControl.close();
		standardOutput.removeListener(standardOutputListener);
		otherOutputControl.close();
		jobProperties.close();
		dataUpload.close();
	}

	// -- Helper methods --

	private void setActiveFirstVisibleTab(final boolean force) {

		if (!force && !getSelectionModel().getSelectedItem().isDisabled()) {
			return;
		}

		for (final Tab t : getTabs()) {
			if (!t.isDisable()) {
				t.getTabPane().getSelectionModel().select(t);
				break;
			}
		}

	}
}
