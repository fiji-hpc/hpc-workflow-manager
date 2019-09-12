
package cz.it4i.fiji.hpc_workflow.ui;

import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.python.jline.internal.Log;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.hpc_workflow.core.MacroTask;
import cz.it4i.fiji.hpc_workflow.core.ObservableHPCWorkflowJob;
import javafx.application.Platform;
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

public class MacroTaskProgressViewController extends BorderPane implements
	CloseableControl, InitiableControl
{

	@FXML
	private TableView<MacroTask> tasksTableView;

	@FXML
	private TableColumn<MacroTask, String> descriptionColumn;

	@FXML
	private Label statusLabel;

	private ObservableHPCWorkflowJob job;

	private ObservableList<MacroTask> tableData = FXCollections
		.observableArrayList();

	ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

	// Maps task id of a specific node to description:
	private List<Map<Integer, String>> nodeTaskToDescription = new ArrayList<>();

	// Maps description to taskId:
	private Map<String, Integer> descriptionToTaskId = new HashMap<>();

	// Maps description to map of node index to observable property:
	private Map<String, Map<Integer, SimpleLongProperty>> descriptionToProperty =
		new HashMap<>();

	final CountDownLatch latchToWaitForJavaFx = new CountDownLatch(1);

	public MacroTaskProgressViewController() {
		init();
	}

	private void init() {
		JavaFXRoutines.initRootAndController("MacroTaskProgressView.fxml", this);
	}

	@Override
	public void init(Window parameter) {
		// This is empty as the parameter is not used.
	}

	private class ProgressCell extends TableCell<MacroTask, Long> {

		final ProgressIndicator cellProgress = new ProgressIndicator();

		// Display progress indicator if the row is not empty:
		@Override
		protected void updateItem(Long t, boolean empty) {
			super.updateItem(t, empty);
			if (!empty && t >= 0) {
				setText(null);
				cellProgress.setProgress(t.doubleValue() / 100D);
				setGraphic(cellProgress);
			}
			else {
				setText(null);
				setGraphic(null);
			}
		}
	}

	@Override
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
			Platform.runLater(() -> {
				createColumnsForEachNode(files.size());
				latchToWaitForJavaFx.countDown();
			});
			try {
				latchToWaitForJavaFx.await();
			}
			catch (InterruptedException exc) {
				Log.error(exc.getMessage());
				Thread.currentThread().interrupt();
			}
			getAndParseFileUpdateTasks(files);

			if (job.getState() != JobState.Queued && job
				.getState() != JobState.Running && job.getState() != JobState.Submitted)
			{
				setStatusMessage("Stopped updating progress because state is: " + job
					.getState().toString());
				exec.shutdown();
			}

		}, 0, 2, TimeUnit.SECONDS);

	}

	private List<String> generateProgressFileNames() {
		// Get number of nodes from first node's progress file:
		int numberOfNodes = 0;

		List<String> files = new ArrayList<>();

		files.add("progress_0.plog");

		List<String> progressLogs = job.getFileContents(files);
		if (!progressLogs.isEmpty()) {
			String log = progressLogs.get(0);
			String[] lines = splitStringByDelimiter(log, "\n");
			try {
				numberOfNodes = Integer.parseInt(lines[0]);
			}
			catch (NumberFormatException exc) {
				setStatusMessage("Progress log does not list node size, yet!");
			}
		}

		// File names of the progress files for each node:
		for (int i = 1; i < numberOfNodes; i++) {
			String filename = "progress_".concat(String.valueOf(i)).concat(".plog");
			setStatusMessage("Adding file: " + filename);
			files.add(filename);
		}
		return files;
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

	private void getAndParseFileUpdateTasks(List<String> files) {
		setStatusMessage("Downloading the macro progress files...");
		List<String> progressLogs = job.getFileContents(files);
		setStatusMessage("Parsing the macro progress files...");
		parseProgressLogs(progressLogs);
		setStatusMessage("Done parsing the macro progress files.");
	}

	private void parseProgressLogs(List<String> progressLogs) {
		for (int i = nodeTaskToDescription.size(); i < progressLogs.size(); i++) {
			nodeTaskToDescription.add(new HashMap<>());
		}

		int nodeId = 0;
		int taskIdCounter = 0;
		for (String log : progressLogs) {
			String[] logLines = splitStringByDelimiter(log, "\n");

			for (String line : logLines) {
				String[] elements = splitStringByDelimiter(line, ",");
				taskIdCounter = setTaskProgressOrDescriptionFromElements(nodeId,
					elements, taskIdCounter);
			}
			nodeId++;
		}
	}

	private int setTaskProgressOrDescriptionFromElements(int nodeId,
		String[] elements, int taskIdCounter)
	{
		if (elements.length == 2) {
			int taskIdForNode = Integer.parseInt(elements[0]);
			try {
				Long progress = Long.parseLong(elements[1]);
				String description = nodeTaskToDescription.get(nodeId).get(
					taskIdForNode);
				int taskId = descriptionToTaskId.get(description);
				tableData.get(taskId).setProgress(nodeId, progress);
				setDescriptionToPropertyIfPossible(description, nodeId, tableData.get(
					taskId).getProgress(nodeId));
			}
			catch (NumberFormatException exc) {
				String description = elements[1];
				if (!descriptionToTaskId.containsKey(description)) {
					descriptionToTaskId.put(description, taskIdCounter++);
					tableData.add(new MacroTask(description));
				}
				nodeTaskToDescription.get(nodeId).put(taskIdForNode, description);
			}
		}
		return taskIdCounter;
	}

	private void setDescriptionToPropertyIfPossible(String description,
		int nodeId, long progress)
	{
		try {
			this.descriptionToProperty.get(description).get(nodeId).set(progress);
		}
		catch (Exception exc) {
			// Do nothing.
		}
	}

	private String[] splitStringByDelimiter(String stringToSplit,
		String delimiter)
	{
		return stringToSplit.split(delimiter);
	}

	private void setStatusMessage(String message) {
		JavaFXRoutines.runOnFxThread(() -> this.statusLabel.setText(message));
	}
}
