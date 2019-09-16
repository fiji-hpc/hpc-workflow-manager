
package cz.it4i.fiji.hpc_workflow.ui;

import cz.it4i.fiji.hpc_workflow.core.ObservableHPCWorkflowJob;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class JobDetailWindow {

	private Stage stage;

	private JobDetailControl controller;

	public JobDetailWindow(Stage parentStage, ObservableHPCWorkflowJob job) {
		JavaFXRoutines.runOnFxThread(() -> openWindow(job, parentStage));
	}

	public void openWindow(ObservableHPCWorkflowJob job, Stage parentStage) {
		// Open the the window:
		this.controller = new JobDetailControl(job);
		final Scene formScene = new Scene(controller);
		stage = new Stage();
		// Disable modal in order to allow the user to open detailed views of
		// multiple jobs.
		stage.initOwner(null);
		stage.initModality(Modality.NONE);
		stage.setResizable(true);
		stage.setTitle("Job dashboard for job #" + job.getValue().getId());
		stage.setScene(formScene);

		finalizeOnStageClose();
		controller.init(stage);

		stage.show();
	}

	public void finalizeOnStageClose() {
		this.stage.setOnCloseRequest((WindowEvent we) -> this.controller.close());
	}
}
