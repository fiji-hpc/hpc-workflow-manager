
package cz.it4i.fiji.hpc_workflow.ui;

import cz.it4i.fiji.commons.UncaughtExceptionHandlerDecorator;
import cz.it4i.fiji.hpc_workflow.WorkflowParadigm;
import cz.it4i.fiji.hpc_workflow.commands.FileLock;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class HPCWorkflowWindow {

	private Stage stage;
	private HPCWorkflowControl controller;

	public HPCWorkflowWindow(WorkflowParadigm paridigm, FileLock fl,
		UncaughtExceptionHandlerDecorator uehd)
	{
		openWindow(paridigm, fl, uehd);
	}

	public void openWindow(WorkflowParadigm paradigm,
		FileLock fl, UncaughtExceptionHandlerDecorator uehd)
	{
		// Open the the window:
		this.controller = new HPCWorkflowControl(paradigm);
		final Scene formScene = new Scene(controller);
		stage = new Stage();
		stage.initOwner(null);
		stage.initModality(Modality.NONE);
		stage.setResizable(true);
		stage.setTitle(Constants.SUBMENU_ITEM_NAME);
		stage.setScene(formScene);

		// Remember to close the file lock and exceptions:
		finalizeOnStageClose(paradigm, fl, uehd);
		controller.init(stage);

		stage.show();
	}

	public void finalizeOnStageClose(WorkflowParadigm paradigm, FileLock fl,
		UncaughtExceptionHandlerDecorator uehd)
	{
		// On close dispose fl and uehd:
		this.stage.setOnCloseRequest((WindowEvent we) -> {
			fl.close();
			uehd.close();
			controller.close();
			paradigm.close();
		});

	}
}
