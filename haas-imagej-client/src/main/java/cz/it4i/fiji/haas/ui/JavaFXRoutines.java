package cz.it4i.fiji.haas.ui;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.JavaFXRoutines.TableCellAdapter.TableCellUpdater;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public interface JavaFXRoutines {

	Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.ui.CloseableControl.class);

	static public class TableCellAdapter<S, T> extends TableCell<S, T> {
		public interface TableCellUpdater<A, B> {
			void accept(TableCell<?, ?> cell, B value, boolean empty);
		}

		private final TableCellUpdater<S, T> updater;

		public TableCellAdapter(TableCellUpdater<S, T> updater) {
			this.updater = updater;
		}

		@Override
		protected void updateItem(T item, boolean empty) {
			if(empty) {
				this.setText("");
			} else {
				updater.accept(this, item, empty);
			}
		}
	}

	static public class FutureValueUpdater<S, T, U extends CompletableFuture<T>> implements TableCellUpdater<S, U> {

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

	static public class StringValueUpdater<S> implements TableCellUpdater<S, String> {
		@Override
		public void accept(TableCell<?, ?> cell, String value, boolean empty) {
			if (value != null) {
				cell.setText(value);
			} else if (!empty) {
				cell.setText("N/A");
			}
		}
	}

	static void initRootAndController(String string, Object parent) {
		initRootAndController(string, parent, false);
	}

	static void initRootAndController(String string, Object parent, boolean setController) {
		FXMLLoader fxmlLoader = new FXMLLoader(parent.getClass().getResource(string));
		fxmlLoader.setControllerFactory(c -> {
			try {
				if (c.equals(parent.getClass())) {
					return parent;
				} else {
					return c.newInstance();
				}
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
		fxmlLoader.setRoot(parent);
		if (setController) {
			fxmlLoader.setController(parent);
		}
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		if (fxmlLoader.getController() == null) {
			throw new IllegalStateException("Not set controller.");
		}

	}

	@SuppressWarnings("unchecked")
	static public <U, T extends ObservableValue<U>, V> void setCellValueFactory(TableView<T> tableView, int index,
			Function<U, V> mapper) {
		((TableColumn<T, V>) tableView.getColumns().get(index))
				.setCellValueFactory(f -> new ObservableValueAdapter<U, V>(f.getValue(), mapper));

	}

	static public void runOnFxThread(Runnable runnable) {
		if (Platform.isFxApplicationThread()) {
			runnable.run();
		} else {
			Platform.runLater(runnable);
		}
	}

	// TODO move to own class in the future
	static public <V> void executeAsync(Executor executor, Callable<V> action, Consumer<V> postAction) {
		executor.execute(() -> {
			V result;
			try {
				result = action.call();
				postAction.accept(result);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		});
	}
	
	public static <T> boolean notNullValue(ObservableValue<T> j, Predicate<T> pred) {
		if (j == null || j.getValue() == null) {
			return false;
		} else {
			return pred.test(j.getValue());
		}
	}

}
