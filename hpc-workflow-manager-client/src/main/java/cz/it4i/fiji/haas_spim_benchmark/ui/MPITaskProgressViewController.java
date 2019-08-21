
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas_spim_benchmark.core.MPITask;
import cz.it4i.fiji.haas_spim_benchmark.core.SimpleObservableValue;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

public class MPITaskProgressViewController extends BorderPane implements
	CloseableControl, InitiableControl
{

	@FXML
	private TableView<MPITask> tasksTableView;

	@FXML
	private TableColumn<MPITask, String> descriptionColumn;

	private Window root;

	private ObservableList<MPITask> tableData = FXCollections
		.observableArrayList();

	public MPITaskProgressViewController() {
		init();
	}

	private void init() {
		JavaFXRoutines.initRootAndController("MPITaskProgressView.fxml", this);
	}

	@Override
	public void init(Window parameter) {
		this.root = parameter;

		// Add some fake tasks:
		tableData.addAll(new MPITask("An empty task."), new MPITask(
			"A second one."));

		// initialize table columns:
		// Get task descriptions:
		this.descriptionColumn.setCellValueFactory(new PropertyValueFactory<>(
			"description"));
		// Create columns for the progress of each node:
		Platform.runLater(() -> createColumnsForEachNode());
		// Add process indicators:
		

		tasksTableView.setItems(tableData);
	}

	private void createColumnsForEachNode() {
		int numberOfNodes = 2;
		for (int i = 0; i < numberOfNodes; i++) {
			TableColumn<MPITask, Long> tempColumn = new TableColumn<>("Node " + i +
				" progress (%)");
			final int index = i;
			tempColumn.setCellValueFactory(cellData -> new SimpleObservableValue<>(
				cellData.getValue().getProgress(index)));
			tempColumn.setCellFactory(e -> new ProgressCell(e.getCellData(index)));
			tasksTableView.getColumns().add(tempColumn);	
		}
	}
	
	private class ProgressCell extends TableCell<MPITask, Long> {

		final ProgressIndicator cellProgress = new ProgressIndicator();

		ProgressCell(double progress) {
			cellProgress.setProgress(progress);
		}

		// Display progress indicator if the row is not empty:
		@Override
		protected void updateItem(Long t, boolean empty) {
			super.updateItem(t, empty);
			if (!empty) {
				setText(null);
				setGraphic(cellProgress);
			} else {
				setText(null);
				setGraphic(null);
			}
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
	}
}
