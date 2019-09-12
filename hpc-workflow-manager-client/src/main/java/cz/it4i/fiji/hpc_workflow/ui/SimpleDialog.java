
package cz.it4i.fiji.hpc_workflow.ui;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SimpleDialog {

	private SimpleDialog() {
		// Empty private constructor to prevent creation of new object.
	}

	public static void showError(String header, String message) {
		showAlert(AlertType.ERROR, header, message);
	}

	public static void showInformation(String header, String message) {
		showAlert(AlertType.INFORMATION, header, message);
	}

	private static void showAlert(AlertType type, String header, String message) {
		Alert alert = new Alert(type);
		alert.setTitle("Error Dialog");
		alert.setHeaderText(header);
		alert.setContentText(message);

		alert.showAndWait();
	}

	public static File fileChooser(Stage stage, String title) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		File selectedFile = fileChooser.showOpenDialog(stage);
		if (selectedFile != null) {
			return selectedFile;
		}
		return null;
	}

	public static File directoryChooser(Stage stage, String title) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle(title);
		File selectedFile = directoryChooser.showDialog(stage);
		if (selectedFile != null) {
			return selectedFile;
		}
		return null;
	}

	public static void showException(String header, String message,
		Exception ex)
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Exception");
		alert.setHeaderText(header);
		alert.setContentText(message);

		// Create expandable Exception text field.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("The exception stacktrace was:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		alert.showAndWait();
	}
}
