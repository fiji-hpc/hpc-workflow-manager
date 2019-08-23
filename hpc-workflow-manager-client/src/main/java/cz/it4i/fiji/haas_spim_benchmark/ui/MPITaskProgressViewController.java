
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.python.jline.internal.Log;

import cz.it4i.fiji.haas.Job;
import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas_java_client.JobState;
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

	private Job job;

	private ObservableList<MPITask> tableData = FXCollections
		.observableArrayList();

	ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

	// Maps task id of a specific node to description:
	private List<Map<Integer, String>> nodeTaskToDescription = new ArrayList<>();

	// Maps description to taskId:
	private Map<String, Integer> descriptionToTaskId = new HashMap<>();

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

	public void setJobParameter(Job newJob) {
		this.job = newJob;

		// initialize table columns:
		// Get task descriptions:
		this.descriptionColumn.setCellValueFactory(new PropertyValueFactory<>(
			"description"));

		tasksTableView.setItems(tableData);

		exec.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				List<String> files = generateProgressFileNames();
				Platform.runLater(() -> createColumnsForEachNode(files.size()));
				getAndParseFileUpdateTasks(files);
				if (job.getState() != JobState.Queued && job
					.getState() != JobState.Running && job
						.getState() != JobState.Submitted)
				{
					Log.info("Stoped updating progress because state is: " + job
						.getState().toString());
					exec.shutdown();
				}
			}
		}, 0, 1, TimeUnit.SECONDS);

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
				Log.error("Incorrect progress log file!" + exc);
			}
		}

		// File names of the progress files for each node:
		for (int i = 1; i < numberOfNodes; i++) {
			String filename = "progress_".concat(String.valueOf(i)).concat(".plog");
			Log.info("Adding file: " + filename);

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
				TableColumn<MPITask, Long> tempColumn = new TableColumn<>("Node " + i +
					" progress (%)");
				final int index = i;
				tempColumn.setCellValueFactory(cellData -> new SimpleObservableValue<>(
					cellData.getValue().getProgress(index)));
				tempColumn.setCellFactory(e -> new ProgressCell(e.getCellData(index)));
				tasksTableView.getColumns().add(tempColumn);
			}
		}
	}

	private void getAndParseFileUpdateTasks(List<String> files) {
		Log.info("Getting the files...");
		List<String> progressLogs = job.getFileContents(files);
		Log.info("Parsing the files...");
		parseProgressLogs(progressLogs);
		Log.info("Done.");
	}

	private void parseProgressLogs(List<String> progressLogs) {

		for (int i = 0; i < progressLogs.size(); i++) {
			nodeTaskToDescription.add(new HashMap<>());
		}

		int nodeId = 0;
		int taskIdCounter = 0;
		for (String log : progressLogs) {
			String[] logLines = splitStringByDelimiter(log, "\n");

			for (String line : logLines) {
				String[] element = splitStringByDelimiter(line, ",");

				if (element.length == 2) {
					int taskIdForNode = Integer.parseInt(element[0]);
					try {
						Long progress = Long.parseLong(element[1]);
						String description = nodeTaskToDescription.get(nodeId).get(
							taskIdForNode);
						int taskId = descriptionToTaskId.get(description);
						tableData.get(taskId).setProgress(nodeId, progress);
					}
					catch (NumberFormatException exc) {
						String description = element[1];
						if (!descriptionToTaskId.containsKey(description)) {
							descriptionToTaskId.put(description, taskIdCounter++);
							tableData.add(new MPITask(description));
						}
						nodeTaskToDescription.get(nodeId).put(taskIdForNode, description);
					}
				}
			}
			nodeId++;
		}
	}

	private String[] splitStringByDelimiter(String stringToSplit,
		String delimiter)
	{
		return stringToSplit.split(delimiter);
	}
}
