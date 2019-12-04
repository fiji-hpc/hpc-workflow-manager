
package cz.it4i.fiji.hpc_workflow.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.hpc_adapter.ui.TableCellAdapter;
import cz.it4i.fiji.hpc_client.data_transfer.FileTransferInfo;
import cz.it4i.fiji.hpc_client.data_transfer.FileTransferState;
import cz.it4i.fiji.hpc_workflow.core.SimpleObservableList;
import cz.it4i.fiji.hpc_workflow.core.SimpleObservableValue;
import cz.it4i.swing_javafx_ui.CloseableControl;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
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

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_workflow.ui.DataTransferController.class);

	@FXML
	private TableView<FileTransferInfo> files;

	public DataTransferController() {
		JavaFXRoutines.initRootAndController(FXML_FILE_NAME, this);
	}

	@SuppressWarnings("unchecked")
	public void setObservable(
		final SimpleObservableList<FileTransferInfo> fileTransferList)
	{

		if (fileTransferList != null && !fileTransferList.isEmpty()) {

			final int columnIndexPath = 0;
			final int columnIndexState = 1;

			JavaFXRoutines.setCellValueFactoryForList(files, columnIndexPath,
				f -> new SimpleObservableValue<>(f.getValue().getFileNameAsString()));
			JavaFXRoutines.setCellValueFactoryForList(files, columnIndexState,
				f -> new SimpleObservableValue<>(f.getValue().getState()));

			final TableColumn<FileTransferInfo, FileTransferState> stateColumn =
				(TableColumn<FileTransferInfo, FileTransferState>) files.getColumns()
					.get(columnIndexState);

			stateColumn.setCellFactory(column -> new TableCellAdapter<>((cell, val,
				empty) -> {
				if (val == null || empty) {
					return;
				}

				final TableRow<FileTransferInfo> currentRow = cell.getTableRow();
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

			files.setItems(fileTransferList);
		}
	}

	// -- CloseableControl methods --

	@Override
	public void close() {
		// Do nothing
	}

}
