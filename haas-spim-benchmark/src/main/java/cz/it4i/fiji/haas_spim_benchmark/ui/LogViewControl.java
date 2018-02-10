package cz.it4i.fiji.haas_spim_benchmark.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.FXFrameNative;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public class LogViewControl extends BorderPane implements CloseableControl {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.LogViewControl.class);

	public LogViewControl() {
		CloseableControl.initRootAndController("LogView.fxml", this);
	}

	public static void main(String[] args) {

		class Window extends FXFrameNative<LogViewControl> {

			public Window() {
				super(() -> new LogViewControl());

			}

		}

		Window w;
		w = new Window();
		w.setVisible(true);

	}

	@FXML
	private ScrollPane sp;

	@FXML
	private BorderPane bp;

	@FXML
	private TextArea ta;

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
