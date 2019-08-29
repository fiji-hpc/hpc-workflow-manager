
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.WindowConstants;

import net.imagej.updater.util.Progress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas.ui.ModalDialogs;
import cz.it4i.fiji.haas.ui.ProgressDialog;
import cz.it4i.fiji.haas_java_client.FileTransferInfo;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.haas_spim_benchmark.core.ObservableBenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.SimpleObservableList;
import cz.it4i.fiji.haas_spim_benchmark.core.SimpleObservableValue;
import cz.it4i.fiji.haas_spim_benchmark.core.Task;
import cz.it4i.fiji.haas_spim_benchmark.ui.NewJobController.WorkflowType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class JobDetailControl extends TabPane implements CloseableControl,
	InitiableControl
{

	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_spim_benchmark.ui.JobDetailControl.class);

	@FXML
	private MacroTaskProgressViewController macroProgressControl;

	@FXML
	private SPIMPipelineProgressViewController progressControl;

	@FXML
	private Tab macroProgressTab;

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
	private DataTransferController dataUploadControl;

	@FXML
	private Tab dataUploadTab;

	private final ExecutorService executorServiceWS;

	private final ObservableBenchmarkJob job;

	private SimpleObservableList<Task> taskList;

	private final ListChangeListener<Task> taskListListener =
		new ListChangeListener<Task>()
		{

			@Override
			public void onChanged(Change<? extends Task> c) {
				setTabAvailability(progressTab, taskList == null || taskList.isEmpty());
			}

		};

	private SimpleObservableValue<String> errorOutput;

	private final ChangeListener<String> errorOutputListener = (
		ObservableValue<? extends String> observable, String oldValue,
		String newValue) -> {
		if (newValue != null) {
			setTabAvailability(snakemakeOutputTab, newValue.isEmpty());
		}
	};

	private SimpleObservableValue<String> standardOutput;

	private final ChangeListener<String> standardOutputListener = (
		ObservableValue<? extends String> observable, String oldValue,
		String newValue) -> {
		if (newValue != null) {
			setTabAvailability(otherOutputTab, newValue.isEmpty());
		}
	};

	public JobDetailControl(final ObservableBenchmarkJob job) {
		executorServiceWS = Executors.newSingleThreadExecutor();
		JavaFXRoutines.initRootAndController("JobDetail.fxml", this);
		this.job = job;
	}

	// -- InitiableControl methods --

	@Override
	public void init(final Window parameter) {

		Progress progress = ModalDialogs.doModal(new ProgressDialog(parameter,
			"Downloading tasks"), WindowConstants.DO_NOTHING_ON_CLOSE);

		executorServiceWS.execute(() -> {

			try {
				WorkflowType jobType = WorkflowType.forLong(job.getJob()
					.getHaasTemplateId());

				if (jobType == WorkflowType.SPIM_WORKFLOW) {
					setTabAvailability(macroProgressTab, true);
					removeTab(macroProgressTab);
					
					// SPIM-only related initializations:
					progressControl.init(parameter);
					taskList = job.getObservableTaskList();
					taskList.subscribe(taskListListener);
					progressControl.setObservable(taskList);
					
					errorOutput = job.getObservableSnakemakeOutput(
						SynchronizableFileType.StandardErrorFile);
					errorOutput.addListener(errorOutputListener);
					snakemakeOutputControl.setObservable(errorOutput);
				}
				else {
					setTabAvailability(macroProgressTab, false);
					setTabAvailability(progressTab, true);
					setTabAvailability(snakemakeOutputTab, true);

					removeTab(progressTab);
					removeTab(snakemakeOutputTab);
					
					// Macro-only related initializations:
					macroProgressControl.init(parameter);
					macroProgressControl.setJobParameter(job.getJob());

					progress.done();
				}
				
				standardOutput = job.getObservableSnakemakeOutput(
					SynchronizableFileType.StandardOutputFile);
				standardOutput.addListener(standardOutputListener);
				otherOutputControl.setObservable(standardOutput);

				jobProperties.setJob(job);

				SimpleObservableList<FileTransferInfo> fileTransferList = job
					.getFileTransferList();
				setTabAvailability(dataUploadTab, fileTransferList == null ||
					fileTransferList.isEmpty());
				dataUploadControl.setObservable(fileTransferList);

				if (job.getValue().getState() == JobState.Disposed && log
					.isInfoEnabled())
				{
					// TODO: Handle this?
					log.info("Job " + job.getValue().getId() +
						" state has been resolved as Disposed.");
				}

				setActiveFirstVisibleTab(true);
			}
			finally {
				final ListChangeListener<Task> localListener =
					new ListChangeListener<Task>()
					{

						@Override
						public void onChanged(Change<? extends Task> c) {
							taskList.unsubscribe(this);
							progress.done();
						}
					};
				WorkflowType jobType = WorkflowType.forLong(job.getJob()
					.getHaasTemplateId());
				if (jobType == WorkflowType.SPIM_WORKFLOW) {
					taskList.subscribe(localListener);
				}
			}
		});

	}

	// -- CloseableControl methods --

	@Override
	public void close() {
		WorkflowType jobType = WorkflowType.forLong(job.getJob()
			.getHaasTemplateId());

		executorServiceWS.shutdown();

		// Close controllers
		if (jobType == WorkflowType.SPIM_WORKFLOW) {
			taskList.unsubscribe(taskListListener);
			progressControl.close();
			errorOutput.removeListener(errorOutputListener);
			snakemakeOutputControl.close();
		}
		else {
			macroProgressControl.close();
		}
		standardOutput.removeListener(standardOutputListener);
		otherOutputControl.close();
		jobProperties.close();
		dataUploadControl.close();
	}

	// -- Helper methods --

	private void setTabAvailability(final Tab tab, final boolean isDisabled) {
		tab.setDisable(isDisabled);
		setActiveFirstVisibleTab(false);
	}
	
	private void removeTab(final Tab tab) {
		JavaFXRoutines.runOnFxThread( () -> this.getTabs().remove(tab));
	}

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
