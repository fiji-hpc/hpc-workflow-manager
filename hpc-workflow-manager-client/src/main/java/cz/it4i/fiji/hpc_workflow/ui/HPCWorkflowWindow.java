
package cz.it4i.fiji.hpc_workflow.ui;

import org.kordamp.ikonli.materialdesign.MaterialDesign;
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
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class HPCWorkflowWindow {

	@Parameter
	private Context context;

	public static boolean windowIsOpen = false;

	public HPCWorkflowWindow() {
		Platform.setImplicitExit(false);
	}

	public <T extends JobWithDirectorySettings> void openWindow(
		WorkflowParadigm<T> paradigm)
	{
		Stage stage;
		HPCWorkflowControl<?> controller;
		if (!windowIsOpen) {
			// Open the the window:
			windowIsOpen = true;
			controller = new HPCWorkflowControl<>(paradigm);
			context.inject(controller);
			final Scene formScene = new Scene(controller);
			stage = new Stage();
			stage.initOwner(null);
			stage.initModality(Modality.NONE);
			stage.setResizable(true);
			stage.setTitle(Constants.SUBMENU_ITEM_NAME);
			stage.setScene(formScene);
			stage.setOnCloseRequest((WindowEvent we) -> {
				controller.close();
				windowIsOpen = false;
			});
			Image myImage = IconHelperMethods.convertIkonToImage(
				MaterialDesign.MDI_ANIMATION);
			stage.getIcons().add(myImage);
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
