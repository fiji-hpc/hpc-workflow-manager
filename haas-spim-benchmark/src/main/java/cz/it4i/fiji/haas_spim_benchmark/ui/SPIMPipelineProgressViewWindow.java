package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.io.IOException;

import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;

public class SPIMPipelineProgressViewWindow extends FXFrame<SPIMPipelineProgressViewController> {

	private static final long serialVersionUID = 1L;

	public SPIMPipelineProgressViewWindow(Window applicationFrame,BenchmarkJob job) throws IOException {
		super(applicationFrame, "/cz/it4i/fiji/haas_spim_benchmark/ui/SPIMPipelineProgressView.fxml");
		init(controller->controller.setBenchmarkJob(job));
	}

	
}
