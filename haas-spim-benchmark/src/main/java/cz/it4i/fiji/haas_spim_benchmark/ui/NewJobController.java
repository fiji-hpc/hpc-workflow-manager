package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;

public class NewJobController extends BorderPane implements CloseableControl, InitiableControl {

	public enum DataLocation {
		DEMONSTRATION_ON_SERVER, WORK_DIRECTORY, CUSTOM_DIRECTORY
	}

	private static final Runnable EMPTY_NOTIFIER = () -> {};

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.NewJobController.class);

	@FXML
	private Button bt_create;

	@FXML
	private ToggleGroup tg_inputDataLocation;

	@FXML
	private ToggleGroup tg_outputDataLocation;

	@FXML
	private RadioButton rb_ownInput;

	@FXML
	private RadioButton rb_ownOutput;

	@FXML
	private TextField et_inputDirectory;

	@FXML
	private TextField et_outputDirectory;

	private DataLocation inputDataLocation;

	private DataLocation outputDataLocation;

	private Window ownerWindow;

		private Runnable createPressedNotifier;

	public NewJobController() {
		JavaFXRoutines.initRootAndController("NewJobView.fxml", this);
		getStylesheets().add(getClass().getResource("NewJobView.css").toExternalForm());
		bt_create.setOnMouseClicked(X -> createPressed());
		tg_inputDataLocation.selectedToggleProperty().addListener((v, old, n) -> selected(v, old, n, rb_ownInput));
		tg_outputDataLocation.selectedToggleProperty().addListener((v, o, n) -> selected(v, o, n, rb_ownOutput));
	}

	@Override
	public void close() {
	}

	@Override
	public void init(Window parameter) {
		ownerWindow = parameter;
	}

	public Path getInputDirectory(Path workingDirectory) {
		return getDirectory(inputDataLocation, et_inputDirectory.getText(), workingDirectory);
	}
	
	public Path getOutputDirectory(Path workingDirectory) {
		return getDirectory(outputDataLocation, et_outputDirectory.getText(), workingDirectory);
	}

	public void setCreatePressedNotifier(Runnable createPressedNotifier) {
		if(createPressedNotifier != null) {
			this.createPressedNotifier = createPressedNotifier;
		} else {
			this.createPressedNotifier = EMPTY_NOTIFIER;
		}
	}

	private Path getDirectory(DataLocation dataLocation, String selectedDirectory, Path workingDirectory) {
		switch (dataLocation) {
		case DEMONSTRATION_ON_SERVER:
			return null;
		case WORK_DIRECTORY:
			return workingDirectory;
		case CUSTOM_DIRECTORY:
			return Paths.get(selectedDirectory).toAbsolutePath();
		default:
			throw new UnsupportedOperationException("Not support " + dataLocation);
		}
	}

	private void createPressed() {
		obtainValues();
		if (checkDirectoryLocationIfNeeded()) {
			ownerWindow.setVisible(false);
			ownerWindow.dispose();
			createPressedNotifier.run();
		}
	}

	private boolean checkDirectoryLocationIfNeeded() {
		return checkDataLocationValue(inputDataLocation, et_inputDirectory.getText(), "input")
				&& checkDataLocationValue(outputDataLocation, et_outputDirectory.getText(), "output");

	}

	private boolean checkDataLocationValue(DataLocation dataLocation, String directory, String type) {
		Path directoryPath = Paths.get(directory);
		if (dataLocation == DataLocation.CUSTOM_DIRECTORY && (!Files.exists(directoryPath) || directory.isEmpty())) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Invalid input provided");
			alert.setHeaderText(null);
			String message = !directory.isEmpty() ? "Directory %s for %s not exists"
					: "Directory for %2$s is not selected.";
			alert.setContentText(String.format(message, directoryPath.toAbsolutePath(), type));
			alert.showAndWait();
			return false;
		}
		return true;
	}

	private void obtainValues() {
		inputDataLocation = obtainDataLocation(tg_inputDataLocation);
		outputDataLocation = obtainDataLocation(tg_outputDataLocation);
	}

	private DataLocation obtainDataLocation(ToggleGroup group) {
		int backawardOrderOfSelected = group.getToggles().size()
				- group.getToggles().indexOf(group.getSelectedToggle());
		return DataLocation.values()[DataLocation.values().length - backawardOrderOfSelected];
	}

	private void selected(ObservableValue<? extends Toggle> v, Toggle o, Toggle n, Parent disableIfNotSelected) {
		disableIfNotSelected.getChildrenUnmodifiable().forEach(node -> node.setDisable(n != disableIfNotSelected));
	}

}
