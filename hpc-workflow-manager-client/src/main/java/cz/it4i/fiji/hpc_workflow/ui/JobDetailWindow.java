
package cz.it4i.fiji.hpc_workflow.ui;

import java.awt.Window;

import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.hpc_workflow.core.ObservableHPCWorkflowJob;

public class JobDetailWindow extends FXFrame<JobDetailControl> {

	private static final long serialVersionUID = 1L;

	public JobDetailWindow(Window parentWindow, ObservableHPCWorkflowJob job) {
		super(parentWindow, () -> new JobDetailControl(job));
		setTitle("Job dashboard for job #" + job.getValue().getId());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
}
