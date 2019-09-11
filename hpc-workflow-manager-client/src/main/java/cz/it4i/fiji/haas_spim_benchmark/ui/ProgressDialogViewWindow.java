
package cz.it4i.fiji.haas_spim_benchmark.ui;

import net.imagej.updater.util.UpdateCanceledException;

import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressDialogViewWindow implements ProgressNotifier {

	private ProgressDialogViewController controller;

	private Stage stage;

	// Properties transfered from ProgressDialog class.
	protected long itemLatestUpdate;

	protected long latestUpdate;

	boolean isCanceled;

	String windowTitle = null;

	private void openWindow(String message, Stage parentStage, boolean show) {
		this.controller = new ProgressDialogViewController(message);
		final Scene formScene = new Scene(this.controller);
		stage = new Stage();
		stage.initOwner(parentStage);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initStyle(StageStyle.DECORATED);
		stage.setResizable(false);
		this.windowTitle = message;
		stage.setTitle(message);
		stage.setScene(formScene);
		if (show) {
			stage.show();
		}
	}

	private void closeWindow() {
		this.controller.close();
	}

	private void setTitleToNextIncompleteTask() {
		String taskDescription = this.controller.getFirstNonCompletedTask();
		if (taskDescription != null) {
			
			JavaFXRoutines.runOnFxThread(() -> this.controller.setMessage(
				this.windowTitle + " : " +taskDescription));
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
		this.controller.doneItem(itemDescription);
	}

	@Override
	public void setTitle(final String title) {
		if(this.windowTitle == null) {
			this.windowTitle = title;
		}
		checkIfCanceled();
	}

	protected void setTitle() {
	
		String latestTask = this.controller.getFirstNonCompletedTask();
		if (this.controller.detailsScrollPaneIsVisible() || latestTask == null) {
			JavaFXRoutines.runOnFxThread(() -> this.controller.setMessage(
				this.windowTitle));
		}
		else {
			JavaFXRoutines.runOnFxThread(() -> this.controller.setMessage(
				this.windowTitle + " : " + latestTask));
		}
	}

	@Override
	public void setCount(int count, int total) {
		//
	}

	@Override
	public void addItem(Object item) {
		if (item instanceof String) {
			addItem(item.toString());

			setTitleToNextIncompleteTask();

			// Remove this line:
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void setItemCount(int count, int total) {
		this.controller.setDoneOutOf(count, total);
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
		JavaFXRoutines.runOnFxThread(() -> closeWindow());
	}

	// Method transfered from ProgressDialog class.
	public void cancel() {
		isCanceled = true;
	}

	protected void checkIfCanceled() {
		if (isCanceled) {
			throw new UpdateCanceledException();
		}
	}

	protected boolean updatesTooFast() {
		if (System.currentTimeMillis() - latestUpdate < 50) {
			return true;
		}
		latestUpdate = System.currentTimeMillis();
		return false;
	}

	protected boolean itemUpdatesTooFast() {
		if (System.currentTimeMillis() - itemLatestUpdate < 50) {
			return true;
		}
		itemLatestUpdate = System.currentTimeMillis();
		return false;
	}
}
