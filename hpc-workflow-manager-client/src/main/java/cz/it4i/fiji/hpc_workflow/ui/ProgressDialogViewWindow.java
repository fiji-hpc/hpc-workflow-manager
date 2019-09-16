
package cz.it4i.fiji.hpc_workflow.ui;

import net.imagej.updater.util.UpdateCanceledException;

import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class ProgressDialogViewWindow implements ProgressNotifier {

	private ProgressDialogViewController controller;

	private boolean isCanceled;

	private String windowTitle = null;

	private void openWindow(String message, Stage parentStage, boolean show) {
		this.controller = new ProgressDialogViewController(message);
		final Scene formScene = new Scene(this.controller);
		Stage stage = new Stage();
		stage.initOwner(parentStage);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setAlwaysOnTop(true);
		stage.initStyle(StageStyle.DECORATED);
		stage.setResizable(false);
		this.windowTitle = message;
		stage.setTitle(message);
		stage.setScene(formScene);
		if (show) {
			stage.show();
		}

		// Prevent user from closing with the x button on the window decoration,
		// JavaFX does not provide a method to remove all buttons from the
		// decoration but keep the decoration to have a handle to move the window.
		stage.setOnCloseRequest(WindowEvent::consume);
	}

	private void closeWindow() {
		this.controller.close();
	}

	private void setTitleToNextIncompleteTask() {
		String taskDescription = this.controller.getFirstNonCompletedTask();
		if (taskDescription != null) {
			JavaFXRoutines.runOnFxThread(() -> this.controller.setMessage(
				this.windowTitle + " : " + taskDescription));
		}
	}

	private void checkIfCanceled() {
		if (isCanceled) {
			throw new UpdateCanceledException();
		}
	}

	public ProgressDialogViewWindow(String message, Stage parentStage,
		boolean show)
	{
		JavaFXRoutines.runOnFxThread(() -> openWindow(message, parentStage, show));
	}

	public ProgressDialogViewWindow(String message, Stage parentStage) {
		JavaFXRoutines.runOnFxThread(() -> openWindow(message, parentStage, true));
	}

	public void addItem(String itemDescription) {
		this.controller.addItem(itemDescription);
	}

	public void itemDone(String itemDescription) {
		this.controller.itemDone(itemDescription);
	}

	@Override
	public void setTitle(final String title) {
		if (this.windowTitle == null) {
			this.windowTitle = title;
		}
		checkIfCanceled();
	}

	// Overall progress bar progress:
	@Override
	public void setCount(int count, int total) {
		this.controller.setProgress(count, total);
	}

	@Override
	public void addItem(Object item) {
		if (item instanceof String) {
			addItem(item.toString());

			setTitleToNextIncompleteTask();
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	// Item specific sub-progress bar in detail scroll pane:
	@Override
	public void setItemCount(int count, int total) {
		this.controller.setCurrentItemProgress(count, total);
	}

	@Override
	public void itemDone(Object item) {
		if (item instanceof String) {
			itemDone(item.toString());

			setTitleToNextIncompleteTask();
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void done() {
		JavaFXRoutines.runOnFxThread(this::closeWindow);
	}
}
