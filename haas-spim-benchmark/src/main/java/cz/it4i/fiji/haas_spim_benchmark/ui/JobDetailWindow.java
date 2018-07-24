
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;

import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;

public class JobDetailWindow extends FXFrame<JobDetailControl> {

	private static final long serialVersionUID = 1L;

	public JobDetailWindow(Window parentWindow, BenchmarkJob job) {
		super(parentWindow, () -> {
			return new JobDetailControl(job);
		});
		setTitle("Job dashboard for job #" + job.getId());
	}

}
