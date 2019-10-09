
package cz.it4i.fiji.hpc_workflow.ui;

import cz.it4i.fiji.hpc_workflow.TaskComputation;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class TaskComputationWindow {

	private TaskComputationControl controller;
	
	private Stage stage;

	public TaskComputationWindow(Stage parentStage,
		TaskComputation computation)
	{
		this.controller = new TaskComputationControl(computation);
		openWindow(parentStage);
	}

	public void openWindow(Stage parentStage) {
		// Open the the window:
		final Scene formScene = new Scene(controller);
		this.stage = new Stage();
		this.stage.initOwner(parentStage);
		this.stage.initModality(Modality.APPLICATION_MODAL);
		this.stage.setResizable(false);
		this.stage.setTitle("Task Computation");
		this.stage.setScene(formScene);

		finalizeOnStageClose();
		controller.init(stage);

		this.stage.showAndWait();
	}

	public void finalizeOnStageClose() {
		this.stage.setOnCloseRequest((WindowEvent we) -> this.controller.close());
	}
}
