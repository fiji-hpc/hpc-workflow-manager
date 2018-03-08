package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.util.concurrent.ExecutorService;

import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.haas_spim_benchmark.core.TaskComputation;

public class TaskComputationWindow extends FXFrame<TaskComputationControl> {

	private static final long serialVersionUID = 1L;

	public TaskComputationWindow(Window applicationFrame,TaskComputation computation, ExecutorService scpExecutor) {
		super(applicationFrame,()->new TaskComputationControl(computation, scpExecutor));
		
	}

	
}
