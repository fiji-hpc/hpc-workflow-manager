
package cz.it4i.fiji.haas.ui;

import javafx.scene.control.TableCell;

public class TableCellAdapter<S, T> extends TableCell<S, T> {

	public interface TableCellUpdater<B> {

		void accept(TableCell<?, ?> cell, B value, boolean empty);
	}

	private final TableCellAdapter.TableCellUpdater<T> updater;

	public TableCellAdapter(TableCellAdapter.TableCellUpdater<T> updater) {
		this.updater = updater;
	}

	@Override
	protected void updateItem(T item, boolean empty) {
		if (empty) {
			this.setText("");
		}
		else {
			updater.accept(this, item, empty);
		}
	}
}
