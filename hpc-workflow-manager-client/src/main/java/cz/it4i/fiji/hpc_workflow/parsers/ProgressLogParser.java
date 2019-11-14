
package cz.it4i.fiji.hpc_workflow.parsers;

import java.util.List;
import java.util.Map;

import cz.it4i.fiji.hpc_workflow.core.MacroTask;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.ObservableList;

public interface ProgressLogParser {

	public int getNumberOfNodes(List<String> progressLogs);

	public boolean parseProgressLogs(List<String> progressLogs, long jobStartedtimestamp,
		ObservableList<MacroTask> tableData,
		Map<String, Map<Integer, SimpleLongProperty>> descriptionToProperty);

	// Returns the time-stamp when the progress log file was last updated:
	long getLastUpdatedTimestamp(int rank, List<String> progressLogs);
}
