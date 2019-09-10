
package cz.it4i.fiji.haas_spim_benchmark.ui;

import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressDialogViewWindow implements ProgressNotifier {

	private ProgressDialogViewController controller;

	private Stage stage;

	public void openWindow(String message, Stage parentStage, boolean show) {
		this.controller = new ProgressDialogViewController(message);
		final Scene formScene = new Scene(this.controller);
		stage = new Stage();
		stage.initOwner(parentStage);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setResizable(false);
		stage.setTitle(message);
		stage.setScene(formScene);
		if (show) {
			stage.show();
		}
	}

	public void addItem(String itemDescription) {
		this.controller.addItem(itemDescription);
	}

	public void itemDone(String itemDescription) {
		this.controller.doneItem(itemDescription);
	}

	public void closeWindow() {
		this.controller.close();
	}

	@Override
	public void setTitle(String title) {
		this.stage.setTitle(title);
	}

	@Override
	public void setCount(int count, int total) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addItem(Object item) {
		if (item instanceof String) {
			addItem(item.toString());

			setTitleToNextIncompleteTask();

			// Remove this line:
			System.out.println("Added item: " + item.toString());
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void setItemCount(int count, int total) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void itemDone(Object item) {
		if (item instanceof String) {
			itemDone(item.toString());

			setTitleToNextIncompleteTask();

			System.out.println("Done with item: " + item.toString());
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void done() {
		closeWindow();
	}

	private void setTitleToNextIncompleteTask() {
		String taskDescription = this.controller.getFirstNonCompletedTask();
		if (taskDescription != null) {
			setTitle(taskDescription);
			this.controller.setMessage(taskDescription);
		}
	}
}
