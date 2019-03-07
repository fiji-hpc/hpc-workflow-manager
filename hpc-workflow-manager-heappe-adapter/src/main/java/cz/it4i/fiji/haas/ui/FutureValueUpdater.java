package cz.it4i.fiji.haas.ui;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import cz.it4i.fiji.haas.ui.TableCellAdapter.TableCellUpdater;
import javafx.scene.control.TableCell;

public class FutureValueUpdater<S, T, U extends CompletableFuture<T>> implements TableCellUpdater<S, U> {

	private final TableCellUpdater<S, T> inner;
	private final Executor executor;

	public FutureValueUpdater(TableCellUpdater<S, T> inner, Executor exec) {
		this.inner = inner;
		this.executor = exec;
	}

	@Override
	public void accept(TableCell<?, ?> cell, U value, boolean empty) {
		if (value != null) {
			if (!value.isDone()) {
				inner.accept(cell, null, empty);
			}
			value.thenAcceptAsync(val -> {
				inner.accept(cell, val, empty);
			}, executor);
		} else {
			inner.accept(cell, null, empty);
		}
	}
}