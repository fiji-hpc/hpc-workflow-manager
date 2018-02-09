package cz.it4i.fiji.haas.ui;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public interface CloseableControl extends Closeable{

	public static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.ui.CloseableControl.class);
	
	@Override
	void close() ;
	
	static void initRootAndController(String string, Parent parent) {
		FXMLLoader fxmlLoader = new FXMLLoader(parent.getClass().getResource(string));
		fxmlLoader.setRoot(parent);
		fxmlLoader.setController(parent);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

	}

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
	
	static public void runOnFxThread(Runnable runnable) {
		if (Platform.isFxApplicationThread()) {
			runnable.run();
		} else {
			Platform.runLater(runnable);
		}
	}

	@SuppressWarnings("unchecked")
	static public <U, T extends ObservableValue<U>, V> void setCellValueFactory(TableView<T> tableView, int index,
			Function<U, V> mapper) {
		((TableColumn<T, V>) tableView.getColumns().get(index))
				.setCellValueFactory(f -> new ObservableValueAdapter<U, V>(f.getValue(), mapper));
		// ((TableColumn<T, String>)
		// tableView.getColumns().get(index)).setCellFactory(f->f.set);

	}

}
