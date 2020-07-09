
package cz.it4i.fiji.hpc_workflow.ui;

import org.scijava.Context;
import org.scijava.parallel.Status;
import org.scijava.plugin.Parameter;
import cz.it4i.fiji.hpc_adapter.JobWithDirectorySettings;
import cz.it4i.fiji.hpc_workflow.WorkflowParadigm;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class HPCWorkflowWindow {

	@Parameter
	private Context ctx;

	private Stage stage;

	private HPCWorkflowControl<?> controller;

	private static boolean windowIsOpen = false;

	public HPCWorkflowWindow() {
		Platform.setImplicitExit(false);
	}

	public <T extends JobWithDirectorySettings> void openWindow(
		WorkflowParadigm<T> paradigm)
	{
		if (!windowIsOpen) {
			// Open the the window:
			windowIsOpen = true;
			this.controller = new HPCWorkflowControl<>(paradigm);
			ctx.inject(controller);
			final Scene formScene = new Scene(controller);
			stage = new Stage();
			stage.initOwner(null);
			stage.initModality(Modality.NONE);
			stage.setResizable(true);
			stage.setTitle(Constants.SUBMENU_ITEM_NAME);
			stage.setScene(formScene);
			this.stage.setOnCloseRequest((WindowEvent we) -> {
				controller.close();
				windowIsOpen = false;
			});
			stage.show();
			controller.init(stage).thenAccept((Void v) -> {
				if (paradigm.getStatus() == Status.NON_ACTIVE) {
					JavaFXRoutines.runOnFxThread(stage::hide);
				}
			});
		}
		else {
			SimpleDialog.showWarning(Constants.SUBMENU_ITEM_NAME +
				" is already open.", "Please close the existing window and try again.");
		}
	}
}
