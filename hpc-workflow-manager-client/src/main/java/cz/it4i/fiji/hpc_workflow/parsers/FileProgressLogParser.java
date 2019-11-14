
package cz.it4i.fiji.hpc_workflow.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cz.it4i.fiji.hpc_workflow.core.MacroTask;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.ObservableList;

public class FileProgressLogParser implements ProgressLogParser {

	@Override
	public int getNumberOfNodes(List<String> progressLogs) {
		int numberOfNodes = 0;
		if (!progressLogs.isEmpty()) {
			String log = progressLogs.get(0);
			String[] lines = log.split("\n");
			try {
				numberOfNodes = Integer.parseInt(lines[0]);
				return numberOfNodes;
			}
			catch (NumberFormatException exc) {
				return 0;
			}
		}
		return numberOfNodes;
	}

	// Maps task id of a specific node to description:
	private List<Map<Integer, String>> nodeTaskToDescription = new ArrayList<>();

	// Maps description to taskId:
	private Map<String, Integer> descriptionToTaskId = new HashMap<>();

	@Override
	public boolean parseProgressLogs(List<String> progressLogs,
		ObservableList<MacroTask> tableData,
		Map<String, Map<Integer, SimpleLongProperty>> descriptionToProperty)
	{
		for (int i = nodeTaskToDescription.size(); i < progressLogs.size(); i++) {
			nodeTaskToDescription.add(new HashMap<>());
		}

		int nodeId = 0;
		int taskIdCounter = 0;
		for (String log : progressLogs) {
			String[] logLines = log.split("\n");

			for (String line : logLines) {
				String[] elements = line.split(",");
				taskIdCounter = setTaskProgressOrDescriptionFromElements(nodeId,
					elements, taskIdCounter, tableData, descriptionToProperty);

				// Task counter -1 means parsing the progress log files failed.
				if (taskIdCounter == -1) {
					return false;
				}
			}
			nodeId++;
		}
		return true;
	}

	private int setTaskProgressOrDescriptionFromElements(int nodeId,
		String[] elements, int taskIdCounter, ObservableList<MacroTask> tableData,
		Map<String, Map<Integer, SimpleLongProperty>> descriptionToProperty)
	{
		if (elements.length == 2) {
			int taskIdForNode = Integer.parseInt(elements[0]);
			try {
				Long progress = Long.parseLong(elements[1]);
				String description = nodeTaskToDescription.get(nodeId).get(
					taskIdForNode);
				int taskId = descriptionToTaskId.get(description);
				tableData.get(taskId).setProgress(nodeId, progress);
				setDescriptionToPropertyIfPossible(descriptionToProperty, description,
					nodeId, tableData.get(taskId).getProgress(nodeId));
			}
			catch (NumberFormatException exc) {
				String description = elements[1];
				if (!descriptionToTaskId.containsKey(description)) {
					descriptionToTaskId.put(description, taskIdCounter++);
					tableData.add(new MacroTask(description));
				}
				nodeTaskToDescription.get(nodeId).put(taskIdForNode, description);
			}
			catch (Exception exc) {
				// A catastrophic exception must have occurred, the executor must be
				// stopped:
				JavaFXRoutines.runOnFxThread(() -> SimpleDialog.showException(
					"Exception occurred while parsing progress file!",
					"Progress logs for this Macro Workflow type job appear" +
						" to be corrupted and parsing them caused an exception.", exc));
				return -1;
			}
		}
		return taskIdCounter;
	}

	private void setDescriptionToPropertyIfPossible(
		Map<String, Map<Integer, SimpleLongProperty>> descriptionToProperty,
		String description, int nodeId, long progress)
	{
		try {
			descriptionToProperty.get(description).get(nodeId).set(progress);
		}
		catch (Exception exc) {
			// Do nothing.
		}
	}

	@Override
	public long getLastUpdatedTimestamp(List<String> progressLogs) {
		long timestamp = -1;
		if (!progressLogs.isEmpty()) {
			String log = progressLogs.get(0);
			String[] lines = log.split("\n");
			try {
				// The time-stamp is on the second line of the progress log:
				timestamp = Long.parseLong(lines[1]);
				return timestamp;
			}
			catch (NumberFormatException exc) {
				return timestamp;
			}
		}
		return timestamp;
	}
}
