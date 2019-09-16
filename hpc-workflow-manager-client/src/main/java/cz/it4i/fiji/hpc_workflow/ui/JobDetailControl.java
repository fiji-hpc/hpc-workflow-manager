
package cz.it4i.fiji.hpc_workflow.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.FileTransferInfo;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.hpc_workflow.core.ObservableHPCWorkflowJob;
import cz.it4i.fiji.hpc_workflow.core.SimpleObservableList;
import cz.it4i.fiji.hpc_workflow.core.SimpleObservableValue;
import cz.it4i.fiji.hpc_workflow.core.Task;
import cz.it4i.fiji.hpc_workflow.ui.NewJobController.WorkflowType;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class JobDetailControl extends TabPane
{

	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_workflow.ui.JobDetailControl.class);

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

	private final ObservableHPCWorkflowJob job;

	private SimpleObservableList<Task> taskList;
	
	private Stage stage;

	private final ListChangeListener<Task> taskListListener = (
		Change<? extends Task> c) -> setTabAvailability(progressTab,
			taskList == null || taskList.isEmpty());

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

	public JobDetailControl(final ObservableHPCWorkflowJob job) {
		executorServiceWS = Executors.newSingleThreadExecutor();
		JavaFXRoutines.initRootAndController("JobDetail.fxml", this);
		this.job = job;
	}

	// -- InitiableControl methods --

	public void init(Stage newStage) {
		this.stage = newStage;
		
		ProgressDialogViewWindow progressDialogViewWindow =
			new ProgressDialogViewWindow("Downloading tasks", this.stage);
		executorServiceWS.execute(() -> {

			try {
				WorkflowType jobType = job.getWorkflowType();

				if (jobType == WorkflowType.SPIM_WORKFLOW) {
					setTabAvailability(macroProgressTab, true);
					removeTab(macroProgressTab);

					// SPIM-only related initializations:
					JavaFXRoutines.runOnFxThread(() -> progressControl.init());
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

					removeTab(progressTab);
					JavaFXRoutines.runOnFxThread(() -> snakemakeOutputTab.setText(
						"Error output"));

					// Macro-only related initializations:
					macroProgressControl.setJobParameter(job);

					progressDialogViewWindow.done();
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

				if (job.getValue().getState() == JobState.Disposed) {
					log.info("Job {} state has been resolved as Disposed.", job.getValue()
						.getId());
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
							progressDialogViewWindow.done();
						}
					};
				WorkflowType jobType = job.getWorkflowType();
				if (jobType == WorkflowType.SPIM_WORKFLOW) {
					taskList.subscribe(localListener);
				}
			}
		});

	}

	// -- CloseableControl methods --

	public void close() {
		WorkflowType jobType = job.getWorkflowType();

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
		JavaFXRoutines.runOnFxThread(() -> this.getTabs().remove(tab));
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
