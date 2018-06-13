package cz.it4i.fiji.haas.ui;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

public interface JavaFXRoutines {

	Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.ui.CloseableControl.class);

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

	static public<T,U extends ObservableValue<T>>void setOnDoubleClickAction(TableView<U> tableView ,ExecutorService executorService,Predicate<T> openAllowed, Consumer<T> r) {
		tableView.setRowFactory(tv -> {
			TableRow<U> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					T rowData = row.getItem().getValue();
					if (openAllowed.test(rowData)) {
						executorService.execute(() -> r.accept(rowData));
					}
				}
			});
			return row;
		});
	}

}
