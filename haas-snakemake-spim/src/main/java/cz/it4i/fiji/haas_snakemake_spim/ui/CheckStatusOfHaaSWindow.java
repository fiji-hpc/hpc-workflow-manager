package cz.it4i.fiji.haas_snakemake_spim.ui;

import java.awt.Frame;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import cz.it4i.fiji.haas.JobManager.JobInfo;
import cz.it4i.fiji.haas.ui.FXFrame;
import javafx.application.Platform;

public class CheckStatusOfHaaSWindow extends FXFrame<CheckStatusOfHaaSController> {

	private static final long serialVersionUID = 1L;

	@Parameter
	private Context context;
	
	public CheckStatusOfHaaSWindow(Frame applicationFrame, Context context) {
		super(applicationFrame, "/cz/it4i/fiji/haas_snakemake_spim/ui/CheckStatusOfHaaS.fxml");
		this.context = context;
		init(this::initController);
		this.setResizable(false);
		this.setTitle("Manage status of HaaS jobs");
	}

	public void addJob(JobInfo job) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				getController().addJob(job);
			}
		});
	}

	private void initController(CheckStatusOfHaaSController controller) {
		context.inject(controller);
	}

}
