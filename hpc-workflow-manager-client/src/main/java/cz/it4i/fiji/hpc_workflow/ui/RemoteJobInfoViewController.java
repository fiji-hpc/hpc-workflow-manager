
package cz.it4i.fiji.hpc_workflow.ui;

import java.io.Closeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public class RemoteJobInfoViewController extends BorderPane implements Closeable {

	@FXML
	private TextArea infoTextArea;
	
	private static final String FXML_FILE_NAME = "RemoteJobInfoView.fxml";
	public static final Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.hpc_workflow.ui.RemoteJobInfoViewController.class);

	public RemoteJobInfoViewController() {
		JavaFXRoutines.initRootAndController(FXML_FILE_NAME, this);
		infoTextArea.setText("Please wait");
	}

	public void setRemoteJobInfo(String newRemoteJobInfo) {
		infoTextArea.setText(newRemoteJobInfo);
	}

	@Override
	public void close() {
		// Nothing to close.
	}
}
