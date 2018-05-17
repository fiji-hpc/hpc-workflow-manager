package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class NewJobController extends BorderPane implements CloseableControl, InitiableControl {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.NewJobController.class);

	@FXML
	private Button btnCreate;
	
	private boolean create = false;

	private Window ownerWindow;

	public NewJobController() {
		JavaFXRoutines.initRootAndController("NewJobView.fxml", this);
		getStylesheets().add(getClass().getResource("NewJobView.css").toExternalForm());
		btnCreate.setOnMouseClicked(X -> createPressed());
	}

	private void createPressed() {
		create = true;
		ownerWindow.setVisible(false);
		ownerWindow.dispose();
	}

	@Override
	public void close() {
		log.info("close");
	}

	@Override
	public void init(Window parameter) {
		ownerWindow = parameter;
	}

}
