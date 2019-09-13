
package cz.it4i.fiji.hpc_workflow.ui;

import cz.it4i.fiji.commons.UncaughtExceptionHandlerDecorator;
import cz.it4i.fiji.hpc_workflow.commands.FileLock;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowJobManager;
import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowParameters;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class HPCWorkflowWindow {

	private Stage stage;
	private HPCWorkflowControl controller;

	public HPCWorkflowWindow(HPCWorkflowParameters params, FileLock fl,
		UncaughtExceptionHandlerDecorator uehd)
	{
		openWindow(new HPCWorkflowJobManager(params), fl, uehd);
	}

	public void openWindow(HPCWorkflowJobManager hpcWorkflowJobManager,
		FileLock fl, UncaughtExceptionHandlerDecorator uehd)
	{
		// Open the the window:
		this.controller = new HPCWorkflowControl(
			hpcWorkflowJobManager, stage);
		final Scene formScene = new Scene(controller);
		stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setResizable(false);
		stage.setTitle(Constants.SUBMENU_ITEM_NAME);
		stage.setScene(formScene);

		// Remember to close the file lock and exceptions:
		lockAndOther(fl, uehd);
		controller.init();

		stage.showAndWait();
	}

	public void lockAndOther(FileLock fl,
		UncaughtExceptionHandlerDecorator uehd)
	{
		// On close dispose fl and uehd:
		this.stage.setOnCloseRequest((WindowEvent we) -> {
			fl.close();
			uehd.close();
			controller.close();
		});
		
	}
}
