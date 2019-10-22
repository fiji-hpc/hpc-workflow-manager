
package cz.it4i.fiji.hpc_workflow.ui;

import java.util.List;
import java.util.Map;

import cz.it4i.fiji.hpc_workflow.core.MacroTask;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.ObservableList;

public interface ProgressLogParser {

	public int getNumberOfNodes(List<String> progressLogs);

	public boolean parseProgressLogs(List<String> progressLogs,
		ObservableList<MacroTask> tableData,
		Map<String, Map<Integer, SimpleLongProperty>> descriptionToProperty);
}
