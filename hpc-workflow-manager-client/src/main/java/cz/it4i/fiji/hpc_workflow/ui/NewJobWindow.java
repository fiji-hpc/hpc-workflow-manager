
package cz.it4i.fiji.hpc_workflow.ui;

import java.awt.Window;
import java.nio.file.Path;

import cz.it4i.fiji.haas.ui.FXFrame;

public class NewJobWindow extends FXFrame<NewJobController> {

	private static final long serialVersionUID = 1L;

	public NewJobWindow(Window parentWindow) {
		super(parentWindow, NewJobController::new);
		setTitle("Create job");
	}

	public Path getInputDirectory(Path workingDirectory) {
		return getFxPanel().getControl().getInputDirectory(workingDirectory);
	}

	public Path getOutputDirectory(Path workingDirectory) {
		return getFxPanel().getControl().getOutputDirectory(workingDirectory);
	}

	public void setCreatePressedNotifier(Runnable runnable) {
		getFxPanel().getControl().setCreatePressedNotifier(runnable);
	}
	
	public int getNumberOfNodes() {
		return getFxPanel().getControl().getNumberOfNodes();
	}
	
	public int getHaasTemplateId() {
		return getFxPanel().getControl().getWorkflowType().getHaasTemplateID();
	}
	
}
