
package cz.it4i.fiji.hpc_workflow.ui;

import java.util.HashMap;
import java.util.Map;

import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ProgressDialogViewController extends GridPane {

	@FXML
	Label taskDescriptionLabel;

	@FXML
	ProgressBar taskProgressBar;

	@FXML
	ToggleButton detailsToggleButton;

	@FXML
	ScrollPane detailsScrollPane;

	@FXML
	GridPane subProgressGridPane;

	private Map<String, Boolean> items = new HashMap<>();

	private Map<String, ProgressBar> itemsProgress = new HashMap<>();

	@FXML
	private void toggleDetailsAction() {
		JavaFXRoutines.runOnFxThread(() -> {
			boolean show = detailsScrollPane.isVisible();
			detailsScrollPane.setVisible(!show);
			detailsToggleButton.setText(!show ? "Hide Details" : "Show Details");
			
			// Increase the stage size to fit the details:
			Stage stage = (Stage) taskDescriptionLabel.getScene().getWindow();
			double originalHeight = 150.0;
			if (!show) {
				stage.setHeight(originalHeight + 200.0);
			} else {
				stage.setHeight(originalHeight);
			}
		});
	}

	public ProgressDialogViewController(String description) {
		JavaFXRoutines.initRootAndController("ProgressDialogView.fxml", this);
		this.taskDescriptionLabel.setText(description);

		// Make the details button visible only when there are details to display:
		this.detailsToggleButton.setVisible(false);
	}

	public void close() {
		Stage stage = (Stage) taskDescriptionLabel.getScene().getWindow();
		stage.close();
	}

	public void addItem(String itemDescription) {
		// Now that there is at least an item enable the details button:
		if (!this.detailsToggleButton.isVisible()) {
			this.detailsToggleButton.setVisible(true);
		}

		items.putIfAbsent(itemDescription, false);

		JavaFXRoutines.runOnFxThread(() -> addSubProgress(itemDescription));
	}

	public void addSubProgress(String itemDescription) {
		Label tempLabel = new Label(itemDescription);
		ProgressBar tempProgressBar = new ProgressBar();
		int rowIndex = items.size() - 1;

		// If it is not the first item add a new row to the GridPane first:
		if (items.size() == 1) {
			this.subProgressGridPane.add(tempLabel, 0, 0);
			this.subProgressGridPane.add(tempProgressBar, 1, 0);
		}
		else {
			this.subProgressGridPane.addRow(rowIndex, tempLabel, tempProgressBar);
		}
		itemsProgress.put(itemDescription, tempProgressBar);
	}

	public void itemDone(String itemDescription) {
		items.put(itemDescription, true);
		setCurrentItemProgress(100, 100);
	}

	public String getFirstNonCompletedTask() {
		return items.entrySet().stream().filter(entry -> entry.getValue().equals(
			false)).map(Map.Entry::getKey).findFirst().orElse(null);
	}

	public void setMessage(String message) {
		this.taskDescriptionLabel.setText(message);
	}

	public void setProgress(int current, int total) {
		JavaFXRoutines.runOnFxThread(() -> this.taskProgressBar.setProgress(
			current / (double) total));
	}

	public void setCurrentItemProgress(int currentlyDone, int total) {
		String itemDescription = getFirstNonCompletedTask();
		JavaFXRoutines.runOnFxThread(() -> itemsProgress.get(itemDescription)
			.setProgress(currentlyDone / (double) total));
	}

}
