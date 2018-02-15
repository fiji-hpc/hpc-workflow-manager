package cz.it4i.fiji.haas_spim_benchmark.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;

public class JobDetailControl extends TabPane implements CloseableControl {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.JobDetailControl.class);

	@FXML
	private SPIMPipelineProgressViewController progressView;
	
	public JobDetailControl(BenchmarkJob job) {
		JavaFXRoutines.initRootAndController("JobDetail.fxml", this);
		progressView.setJob(job);
	}



	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
