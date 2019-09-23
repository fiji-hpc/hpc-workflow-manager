
package cz.it4i.fiji.hpc_workflow.ui;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RemoteFilesInfoControl extends BorderPane {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_workflow.ui.RemoteFilesInfoControl.class);

	@SuppressWarnings("unused")
	private Stage root;

	@FXML
	private TableView<ObservableValue<RemoteFileInfo>> files;

	public RemoteFilesInfoControl() {
		JavaFXRoutines.initRootAndController("RemoteFilesInfo.fxml", this);
	}

	public void setFiles(List<ObservableValue<RemoteFileInfo>> files) {
		files.forEach(file -> this.files.getItems().add(file));
	}

	public void init(Stage parentStage) {
		this.root = parentStage;
		initTable();
	}

	private void initTable() {
		JavaFXRoutines.setCellValueFactory(files, 0, RemoteFileInfo::getName);
		JavaFXRoutines.setCellValueFactory(files, 1,
			(Function<RemoteFileInfo, String>) file -> file.getSize() >= 0
				? formatSize(file.getSize()) : "Not exists");
	}

	private String formatSize(long size) {
		return FileUtils.byteCountToDisplaySize(size);
	}

	// This method is used only for testing:
	public void openWindow(Stage parentStage) {
		this.init(parentStage);

		RemoteFilesInfoControl controller = new RemoteFilesInfoControl();

		// Open the the window:
		final Scene formScene = new Scene(controller);
		Stage stage = new Stage();
		stage.initOwner(parentStage);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setResizable(false);
		stage.setTitle("Remote files Info Control");
		stage.setScene(formScene);

		controller.init(stage);

		stage.showAndWait();
	}
}
