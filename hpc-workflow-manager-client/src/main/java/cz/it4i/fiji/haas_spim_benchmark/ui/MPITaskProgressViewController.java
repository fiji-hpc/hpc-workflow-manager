
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import cz.it4i.fiji.haas.Job;
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
	
	private Job job;

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

	public void setJobParameter(Job newJob) {
		this.job = newJob;
		
		// Get the actual data from the progress files:	
		List<String> files = new ArrayList<>();
		for(int i = 0; i < NUMBER_OF_NODES; i++) {
			String filename = "progress_".concat(String.valueOf(i)).concat(".plog");
			System.out.println("Adding file: "+filename);
			files.add(filename);
		}
		
		List<String> progressLogs = job.getFileContents(files);
		
		readProgress(progressLogs);

		// initialize table columns:
		// Get task descriptions:
		this.descriptionColumn.setCellValueFactory(new PropertyValueFactory<>(
			"description"));
		// Create columns for the progress of each node:
		Platform.runLater(() -> createColumnsForEachNode());

		tasksTableView.setItems(tableData);
	}
	
	private void readProgress(List<String> progressLogs) {
		int nodeId = 0;
		for (String log : progressLogs) {
			System.out.println(log);
			String[] logLines = splitStringByDelimiter(log, "\n");
			
			for(String line : logLines) {
				String[] element = splitStringByDelimiter(line, ",");
				if(element.length == 2) {
					int taskId = Integer.parseInt(element[0]);
					try {
						Long progress = Long.parseLong(element[1]);
						tableData.get(taskId).setProgress(nodeId, progress);
					} catch(NumberFormatException exc) {
						if(nodeId == 0) {
							String description = element[1];
							tableData.add(new MPITask(description));
						}
					}					
				} else {
					System.out.println("Incorrect progress log file. Incorrect line: "+line);
				}				
			}
			nodeId++;
		}
	}
	
	private String[] splitStringByDelimiter(String stringToSplit, String delimiter) {
    return stringToSplit.split(delimiter);
	}
}
