
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

import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.NUMBER_OF_NODES;

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
		MPITask fakeMPITask1 = new MPITask("An empty task.");
		fakeMPITask1.setProgress(0, 90L);
		MPITask fakeMPITask2 = new MPITask("A second one.");
		fakeMPITask2.setProgress(0, 99L);
		fakeMPITask2.setProgress(1, 100L);
		tableData.addAll(fakeMPITask1, fakeMPITask2);

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
		for (int i = 0; i < NUMBER_OF_NODES; i++) {
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
			if (!empty && t >= 0) {
				setText(null);
				cellProgress.setProgress(t.doubleValue()/100D);
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
