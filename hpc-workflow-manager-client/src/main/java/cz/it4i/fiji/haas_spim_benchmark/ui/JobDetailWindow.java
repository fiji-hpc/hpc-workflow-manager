
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;

import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.haas_spim_benchmark.core.ObservableBenchmarkJob;

public class JobDetailWindow extends FXFrame<JobDetailControl> {

	private static final long serialVersionUID = 1L;

	public JobDetailWindow(Window parentWindow, ObservableBenchmarkJob job) {
		super(parentWindow, () -> new JobDetailControl(job));
		setTitle("Job dashboard for job #" + job.getValue().getId());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
}
