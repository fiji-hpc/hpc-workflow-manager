
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas.ui.TableCellAdapter;
import cz.it4i.fiji.haas_java_client.FileTransferInfo;
import cz.it4i.fiji.haas_java_client.FileTransferState;
import cz.it4i.fiji.haas_spim_benchmark.core.ObservableBenchmarkJob;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValueBase;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class DataTransferController extends BorderPane implements
	CloseableControl
{

	private static final String FXML_FILE_NAME = "DataTransfer.fxml";

	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_spim_benchmark.ui.DataTransferController.class);

	@FXML
	private TableView<SimpleObservableValue<FileTransferInfo>> files;

	private ObservableBenchmarkJob job;

	private final InvalidationListener listener = new InvalidationListener() {

		@Override
		public void invalidated(final Observable observable) {

			if (!ObservableBenchmarkJob.class.isAssignableFrom(observable
				.getClass()))
			{
				log.debug(
					"Invalidated Observable is not of the ObservableBenchmarkJob type.");
				return;
			}

			files.getItems().clear();

			final List<SimpleObservableValue<FileTransferInfo>> tempList =
				new LinkedList<>();

			((ObservableBenchmarkJob) observable).getValue().getFileTransferInfo()
				.forEach(i -> {
					tempList.add(new SimpleObservableValue<>(i));
				});

			files.getItems().addAll(tempList);
		}

	};

	public DataTransferController() {
		JavaFXRoutines.initRootAndController(FXML_FILE_NAME, this);
		initTable();
	}

	public void setJob(final ObservableBenchmarkJob job) {
		this.job = job;
		this.job.addListener(listener);
		listener.invalidated(job);
	}

	// -- CloseableControl methods --

	@Override
	public void close() {
		job.removeListener(listener);
	}

	// -- Helper methods --

	private void initTable() {

		final int columnIndexPath = 0;
		final int columnIndexState = 1;

		JavaFXRoutines.setCellValueFactory(files, columnIndexPath, f -> f
			.getPathAsString());
		JavaFXRoutines.setCellValueFactory(files, columnIndexState, f -> f
			.getState());

		final TableColumn<SimpleObservableValue<FileTransferInfo>, FileTransferState> stateColumn =
			(TableColumn<SimpleObservableValue<FileTransferInfo>, FileTransferState>) files
				.getColumns().get(columnIndexState);

		stateColumn.setCellFactory(column -> new TableCellAdapter<>((cell, val,
			empty) -> {
			if (val == null || empty) {
				return;
			}
			TableRow<SimpleObservableValue<FileTransferInfo>> currentRow = cell
				.getTableRow();
			cell.setText(val.toString());
			if (val.equals(FileTransferState.Finished)) {
				currentRow.setStyle("-fx-text-background-color: " + JavaFXRoutines
					.toCss(Color.rgb(0x41, 0xB2, 0x80)));
			}
			else {
				currentRow.setStyle("-fx-text-background-color: " + JavaFXRoutines
					.toCss(Color.rgb(0x30, 0xA2, 0xCC)));
			}
		}));
	}

	// -- Private classes --

	private class SimpleObservableValue<T> extends ObservableValueBase<T> {

		private final T wrappedObject;

		public SimpleObservableValue(final T wrappedObject) {
			this.wrappedObject = wrappedObject;
		}

		@Override
		public T getValue() {
			return wrappedObject;
		}

	}

}
