package cz.it4i.fiji.haas.ui;

import cz.it4i.fiji.haas.ui.TableCellAdapter.TableCellUpdater;
import javafx.scene.control.TableCell;

public class StringValueUpdater<S> implements TableCellUpdater<S, String> {
	@Override
	public void accept(TableCell<?, ?> cell, String value, boolean empty) {
		if (value != null) {
			cell.setText(value);
		} else if (!empty) {
			cell.setText("N/A");
		}
	}
}