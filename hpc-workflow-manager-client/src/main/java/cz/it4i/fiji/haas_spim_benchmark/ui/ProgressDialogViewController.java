
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.util.HashMap;
import java.util.Map;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ProgressDialogViewController extends AnchorPane implements
	CloseableControl, InitiableControl
{

	@FXML
	Label taskDescriptionLabel;

	@FXML
	ProgressBar taskProgressBar;

	public Map<String, Boolean> items = new HashMap<>();

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
		return items.entrySet().stream().filter(entry -> entry.getValue().equals(false)).map(
			Map.Entry::getKey).findFirst().orElse(null);
	}

	public void setMessage(String message) {
		this.taskDescriptionLabel.setText(message);
	}

}
