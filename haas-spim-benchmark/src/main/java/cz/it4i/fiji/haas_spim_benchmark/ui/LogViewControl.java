package cz.it4i.fiji.haas_spim_benchmark.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public class LogViewControl extends BorderPane implements CloseableControl {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.LogViewControl.class);

	public LogViewControl() {
		JavaFXRoutines.initRootAndController("LogView.fxml", this);
	}

	@FXML
	private TextArea ta;

	public void setObservable(ObservableValue<String> value) {
		JavaFXRoutines.runOnFxThread(()->ta.setText(value.getValue()));
		value.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				JavaFXRoutines.runOnFxThread(()->ta.setText(value.getValue()));
			}
		});
	}
	
	@Override
	public void close() {
		//DO NOTHING
	}
}
