
package cz.it4i.fiji.hpc_workflow.ui;

import java.nio.file.Path;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class NewJobWindow {

	private NewJobController controller;
	private Stage stage;

	public NewJobWindow(Stage stage) {
		this.stage = stage;
		this.controller = new NewJobController();
	}

	public Path getInputDirectory(Path workingDirectory) {
		return this.controller.getInputDirectory(workingDirectory);
	}

	public Path getOutputDirectory(Path workingDirectory) {
		return this.controller.getOutputDirectory(workingDirectory);
	}

	public void setCreatePressedNotifier(Runnable runnable) {
		this.controller.setCreatePressedNotifier(runnable);
	}

	public int getNumberOfNodes() {
		return this.controller.getNumberOfNodes();
	}

	public int getHaasTemplateId() {
		return this.controller.getWorkflowType().getHaasTemplateID();
	}

	public void openWindow(Stage parentStage) {
		// Open the the window:		
		final Scene formScene = new Scene(controller);
		this.stage = new Stage();
		this.stage.initOwner(parentStage);
		this.stage.initModality(Modality.APPLICATION_MODAL);
		this.stage.setResizable(false);
		this.stage.setTitle("Create job");
		this.stage.setScene(formScene);

		finalizeOnStageClose();
		controller.init(stage);

		this.stage.showAndWait();
	}

	public void finalizeOnStageClose() {
		this.stage.setOnCloseRequest((WindowEvent we) -> this.controller.close());
	}

	public String getUserScriptName() {
		return this.controller.getUserScriptName();
	}
}
