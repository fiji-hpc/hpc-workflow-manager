
package cz.it4i.fiji.hpc_adapter.ui;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import cz.it4i.fiji.hpc_adapter.ui.TableCellAdapter.TableCellUpdater;
import javafx.scene.control.TableCell;

public class FutureValueUpdater<T, U extends CompletableFuture<T>> implements
	TableCellUpdater<U>
{

	private final TableCellUpdater<T> inner;
	private final Executor executor;

	public FutureValueUpdater(TableCellUpdater<T> inner, Executor exec) {
		this.inner = inner;
		this.executor = exec;
	}

	@Override
	public void accept(TableCell<?, ?> cell, U value, boolean empty) {
		if (value != null) {
			if (!value.isDone()) {
				inner.accept(cell, null, empty);
			}
			value.thenAcceptAsync(val -> inner.accept(cell, val, empty), executor);
		}
		else {
			inner.accept(cell, null, empty);
		}
	}
}
