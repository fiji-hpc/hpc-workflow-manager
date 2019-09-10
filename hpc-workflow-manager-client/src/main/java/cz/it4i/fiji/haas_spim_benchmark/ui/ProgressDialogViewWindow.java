
package cz.it4i.fiji.haas_spim_benchmark.ui;

import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProgressDialogViewWindow implements ProgressNotifier {

	private ProgressDialogViewController controller;

	public void openWindow(String message, boolean show) {
		this.controller = new ProgressDialogViewController(message);
		final Scene formScene = new Scene(this.controller);
		Stage parentStage = new Stage();
		parentStage.initModality(Modality.APPLICATION_MODAL);
		parentStage.setResizable(false);
		parentStage.setTitle(message);
		parentStage.setScene(formScene);
		if (show) {
			parentStage.show();
		}
	}

	public void addItem(String itemDescription) {
		this.controller.newItem(itemDescription);
	}

	public void itemDone(String itemDescription) {
		this.controller.updateItem(itemDescription);
	}

	public void closeWindow() {
		this.controller.close();
	}

	@Override
	public void setTitle(String title) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCount(int count, int total) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addItem(Object item) {
		if(item instanceof String) {
			addItem(item.toString());
		} else {
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
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void done() {
		closeWindow();
	}
}
