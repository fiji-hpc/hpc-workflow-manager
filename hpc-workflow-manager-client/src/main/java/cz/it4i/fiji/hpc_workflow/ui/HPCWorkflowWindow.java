package cz.it4i.fiji.hpc_workflow.ui;

import java.awt.Window;

import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.hpc_workflow.core.BenchmarkJobManager;
import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowParameters;


public class HPCWorkflowWindow extends FXFrame<HPCWorkflowControl>{

	private static final long serialVersionUID = 1L;
		
	public HPCWorkflowWindow(Window parentWindow, HPCWorkflowParameters params) {
		super(parentWindow, () -> new HPCWorkflowControl(new BenchmarkJobManager(
			params)));
		
	}
}
