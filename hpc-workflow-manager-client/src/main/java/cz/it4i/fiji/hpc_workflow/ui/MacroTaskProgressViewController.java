
package cz.it4i.fiji.hpc_workflow.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import cz.it4i.fiji.hpc_client.JobState;
import cz.it4i.fiji.hpc_workflow.core.MacroTask;
import cz.it4i.fiji.hpc_workflow.core.ObservableHPCWorkflowJob;
import cz.it4i.fiji.hpc_workflow.parsers.FileProgressLogParser;
import cz.it4i.fiji.hpc_workflow.parsers.ProgressLogParser;
import cz.it4i.fiji.hpc_workflow.parsers.XmlProgressLogParser;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

public class MacroTaskProgressViewController extends BorderPane {

	@FXML
	private TableView<MacroTask> tasksTableView;

	@FXML
	private TableColumn<MacroTask, String> descriptionColumn;

	@FXML
	private Label statusLabel;

	private ProgressLogParser progressLogParser = null;

	private ObservableHPCWorkflowJob job;

	private ObservableList<MacroTask> tableData = FXCollections
		.observableArrayList();

	private ScheduledExecutorService exec = Executors
		.newSingleThreadScheduledExecutor();

	// Maps description to map of node index to observable property:
	private Map<String, Map<Integer, SimpleLongProperty>> descriptionToProperty =
		new HashMap<>();

	public MacroTaskProgressViewController() {
		init();
	}

	private void init() {
		JavaFXRoutines.initRootAndController("MacroTaskProgressView.fxml", this);
	}

	private class ProgressCell extends TableCell<MacroTask, Long> {

		final ProgressIndicator cellProgress = new ProgressIndicator();

		// Display progress indicator if the row is not empty:
		@Override
		protected void updateItem(Long t, boolean empty) {
			super.updateItem(t, empty);
			if (!empty && t >= -1) {
				setText(null);
				if (t == -1) {
					cellProgress.setProgress(-1);
				}
				else {
					cellProgress.setProgress(t.doubleValue() / 100D);
				}
				setGraphic(cellProgress);
			}
			else {
				setText(null);
				setGraphic(null);
			}
		}
	}

	public void close() {
		exec.shutdown();
	}

	public void setJobParameter(ObservableHPCWorkflowJob newJob) {
		this.job = newJob;

		// Initialize table columns:
		// Task description column:
		this.descriptionColumn.setCellValueFactory(new PropertyValueFactory<>(
			"description"));

		tasksTableView.setItems(tableData);

		exec.scheduleAtFixedRate(() -> {
			List<String> files = generateProgressFileNames();
			if (!files.isEmpty()) {
				JavaFXRoutines.runOnFxThread(() -> createColumnsForEachNode(files
					.size()));
				getAndParseFileUpdateTasks(files);

				JobState jobState = job.getState();
				if (jobState != JobState.Queued && jobState != JobState.Running &&
					jobState != JobState.Submitted)
				{
					setStatusMessage("Stopped updating progress because state is: " +
						jobState.toString());
					exec.shutdown();
				}
			}

		}, 0, 2, TimeUnit.SECONDS);

	}

	private List<String> generateProgressFileNames() {
		// Get number of nodes from first node's progress file:
		int numberOfNodes = 0;

		List<String> fileNames = new ArrayList<>();
		List<String> tempFileNames = new ArrayList<>();

		tempFileNames.add("progress_0.plog");
		List<String> progressLogs = job.getFileContents(tempFileNames);

		// Check if the file is created or not yet:
		if (progressLogs.get(0).isEmpty()) {
			setStatusMessage("Progress logs do not exist yet.");
		}
		else {
			fileNames.add("progress_0.plog");
			// Check which progress log format is used: CSV or XML:
			setStatusMessage("Checking type of progress log.");
			if (progressLogParser == null) {
				if (XmlProgressLogParser.isXML(progressLogs.get(0))) {
					progressLogParser = new XmlProgressLogParser();
					setStatusMessage("XML progress log detected.");
				}
				else {
					progressLogParser = new FileProgressLogParser();
					setStatusMessage("CSV progress log detected.");
				}
			}

			setStatusMessage("Getting the number of nodes.");
			numberOfNodes = progressLogParser.getNumberOfNodes(progressLogs);
			if (numberOfNodes == 0) {
				setStatusMessage("Progress log does not list node size, yet!");
			}
			else {
				// File names of the progress files for each node:
				for (int i = 1; i < numberOfNodes; i++) {
					String filename = "progress_".concat(String.valueOf(i)).concat(
						".plog");
					setStatusMessage("Adding progress file: " + filename +
						" in the list.");
					fileNames.add(filename);
				}
			}
		}
		return fileNames;
	}

	private void createColumnsForEachNode(int numberOfNodes) {
		for (int i = 0; i < numberOfNodes; i++) {
			try {
				// If column exists for node do nothing:
				// First column is ignored as it is the task description column.
				tasksTableView.getColumns().get(i + 1);
			}
			catch (IndexOutOfBoundsException exc) {
				TableColumn<MacroTask, Long> tempColumn = new TableColumn<>("Node " +
					i + " progress (%)");
				final int nodeId = i;
				tempColumn.setCellValueFactory(cellData -> createObservableProperty(
					cellData, nodeId).asObject());
				tempColumn.setCellFactory(cell -> new ProgressCell());
				tasksTableView.getColumns().add(tempColumn);
			}
		}
	}

	private SimpleLongProperty createObservableProperty(
		CellDataFeatures<MacroTask, Long> cellData, int nodeId)
	{
		SimpleLongProperty cellValueProperty = new SimpleLongProperty();
		cellValueProperty.setValue(cellData.getValue().getProgress(nodeId));
		Map<Integer, SimpleLongProperty> innerMap;
		String description = cellData.getValue().getDescription();
		if (this.descriptionToProperty.containsKey(description)) {
			innerMap = this.descriptionToProperty.get(description);
			if (!innerMap.containsKey(nodeId)) {
				innerMap.put(Integer.valueOf(nodeId), cellValueProperty);
			}
		}
		else {
			innerMap = new HashMap<>();
			innerMap.put(nodeId, cellValueProperty);
			this.descriptionToProperty.put(description, innerMap);
		}
		return this.descriptionToProperty.get(description).get(nodeId);
	}

	private void setStatusMessage(String message) {
		JavaFXRoutines.runOnFxThread(() -> this.statusLabel.setText(message));
	}

	private void getAndParseFileUpdateTasks(List<String> files) {
		setStatusMessage("Downloading the macro progress files...");
		List<String> progressLogs = job.getFileContents(files);

		setStatusMessage("Parsing the macro progress files...");
		if (!progressLogParser.parseProgressLogs(progressLogs, job
			.getLastStartedTimestamp(), tableData, descriptionToProperty))
		{
			// A catastrophic exception must have occurred, the executor must be
			// stopped:
			setStatusMessage("Catastrophic exception during parsing.");
			exec.shutdown();
		}
		setStatusMessage("Done parsing the macro progress files.");
	}
}
