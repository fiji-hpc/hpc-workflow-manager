package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;

public class JobDetailControl extends TabPane implements CloseableControl {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.JobDetailControl.class);

	@FXML
	private SPIMPipelineProgressViewController progressView;
	
	@FXML
	private LogViewControl errorOutput;
	
	@FXML
	private LogViewControl standardOutput;
	
	private Collection<HaaSOutputObservableValue> observables = new LinkedList<>();
	public JobDetailControl(BenchmarkJob job) {
		JavaFXRoutines.initRootAndController("JobDetail.fxml", this);
		progressView.setJob(job);
		errorOutput.setObservable(addObservable(new HaaSOutputObservableValue(()->job.getStandardError(), Constants.HAAS_UPDATE_TIMEOUT / Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO)));
		standardOutput.setObservable(addObservable(new HaaSOutputObservableValue(()->job.getStandardOutput(), Constants.HAAS_UPDATE_TIMEOUT / Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO)));
	}



	private ObservableValue<String> addObservable(HaaSOutputObservableValue observable) {
		observables.add(observable);
		return observable;
	}



	@Override
	public void close() {
		for (HaaSOutputObservableValue observable : observables) {
			observable.close();
		}
		progressView.close();
	}

}
