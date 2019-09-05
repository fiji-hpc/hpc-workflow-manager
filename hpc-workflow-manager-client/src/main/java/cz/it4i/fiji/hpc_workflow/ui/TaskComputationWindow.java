package cz.it4i.fiji.hpc_workflow.ui;

import java.awt.Window;

import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.hpc_workflow.core.TaskComputation;

public class TaskComputationWindow extends FXFrame<TaskComputationControl> {

	private static final long serialVersionUID = 1L;

	public TaskComputationWindow(Window applicationFrame,TaskComputation computation) {
		super(applicationFrame,()->new TaskComputationControl(computation));
		
	}

	
}
