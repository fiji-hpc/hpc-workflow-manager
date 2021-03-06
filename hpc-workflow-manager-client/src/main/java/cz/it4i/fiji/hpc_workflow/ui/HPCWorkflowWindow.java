
package cz.it4i.fiji.hpc_workflow.ui;

import org.scijava.parallel.Status;

import cz.it4i.fiji.hpc_workflow.WorkflowParadigm;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class HPCWorkflowWindow {

	private Stage stage;
	private HPCWorkflowControl controller;

	public HPCWorkflowWindow() {
		Platform.setImplicitExit(false);
	}

	public void openWindow(WorkflowParadigm paradigm) {
		// Open the the window:
		this.controller = new HPCWorkflowControl(paradigm);
		final Scene formScene = new Scene(controller);
		stage = new Stage();
		stage.initOwner(null);
		stage.initModality(Modality.NONE);
		stage.setResizable(true);
		stage.setTitle(Constants.SUBMENU_ITEM_NAME);
		stage.setScene(formScene);
		this.stage.setOnCloseRequest((WindowEvent we) -> controller.close());
		stage.show();
		controller.init(stage).thenAccept((Void v) -> {
			if (paradigm.getStatus() == Status.NON_ACTIVE) {
				JavaFXRoutines.runOnFxThread(stage::hide);
			}
		});
	}
}
