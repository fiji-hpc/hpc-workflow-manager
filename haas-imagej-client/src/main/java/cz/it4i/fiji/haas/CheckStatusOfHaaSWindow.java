package cz.it4i.fiji.haas;

import java.awt.Frame;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import cz.it4i.fiji.haas.JobManager.JobInfo;
import cz.it4i.fiji.haas.ui.CheckStatusOfHaaSController;

public class CheckStatusOfHaaSWindow extends FXFrame<CheckStatusOfHaaSController> {

	private static final long serialVersionUID = 1L;

	@Parameter
	private Context context;
	
	private CheckStatusOfHaaSController controller;

	private Frame applicationFrame;
	public CheckStatusOfHaaSWindow(Frame applicationFrame, Context context) {
		super(applicationFrame,"/cz/it4i/fiji/haas/ui/CheckStatusOfHaaS.fxml");
		this.context = context;
		init(this::initController);
		this.setResizable(false);
		this.setTitle("Manage status of HaaS jobs");
		this.applicationFrame = applicationFrame;
	}
	
	public void addJob(JobInfo job) {
		controller.addJob(job);
	}
	
	private void initController(CheckStatusOfHaaSController controller) {
		this.controller = controller;
		context.inject(controller);
		controller.init(applicationFrame);
	}

}
