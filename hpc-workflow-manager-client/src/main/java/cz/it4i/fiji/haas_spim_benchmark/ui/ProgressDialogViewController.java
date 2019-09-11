
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.util.HashMap;
import java.util.Map;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ProgressDialogViewController extends AnchorPane implements
	CloseableControl, InitiableControl
{

	@FXML
	Label taskDescriptionLabel;

	@FXML
	ProgressBar taskProgressBar;

	@FXML
	Button detailsButton;

	@FXML
	ScrollPane detailsScrollPane;

	private Map<String, Boolean> items = new HashMap<>();

	public ProgressDialogViewController(String description) {
		JavaFXRoutines.initRootAndController("ProgressDialogView.fxml", this);
		this.taskDescriptionLabel.setText(description);
	}

	@Override
	public void init(Window parameter) {
		// Nothing to initialize.
	}

	@Override
	public void close() {
		Stage stage = (Stage) taskDescriptionLabel.getScene().getWindow();
		stage.close();
	}

	public void addItem(String itemDescription) {
		items.putIfAbsent(itemDescription, false);
	}

	public void doneItem(String itemDescription) {
		items.put(itemDescription, true);
	}

	public String getFirstNonCompletedTask() {
		return items.entrySet().stream().filter(entry -> entry.getValue().equals(
			false)).map(Map.Entry::getKey).findFirst().orElse(null);
	}

	public void setMessage(String message) {
		this.taskDescriptionLabel.setText(message);
	}

	public void setDoneOutOf(int current, int total) {
		this.taskProgressBar.setProgress(current / (double) total);
	}

	public boolean detailsScrollPaneIsVisible() {
		return detailsScrollPane.isVisible();
	}

	@FXML
	private void toggleDetailsAction() {
		boolean show = detailsScrollPane.isVisible();
		detailsScrollPane.setVisible(show);
		detailsButton.setText(show ? "Hide Details" : "Show Details");
	}

}
