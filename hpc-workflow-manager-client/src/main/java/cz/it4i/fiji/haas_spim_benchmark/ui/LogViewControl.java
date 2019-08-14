
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

	@FXML
	private TextArea otherOutputTextArea;

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_spim_benchmark.ui.LogViewControl.class);

	private ObservableValue<String> observedValue;

	private final ChangeListener<String> outputChangeListener;

	public LogViewControl() {
		JavaFXRoutines.initRootAndController("LogView.fxml", this);
		outputChangeListener = new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable,
				String oldValue, String newValue)
			{
				JavaFXRoutines.runOnFxThread(() -> otherOutputTextArea.setText(observedValue
					.getValue()));
			}
		};
	}

	public void setObservable(ObservableValue<String> value) {
		observedValue = value;
		JavaFXRoutines.runOnFxThread(() -> otherOutputTextArea.setText(observedValue.getValue()));
		observedValue.addListener(outputChangeListener);
	}

	@Override
	public void close() {
		observedValue.removeListener(outputChangeListener);
	}
}
