
package cz.it4i.fiji.hpc_workflow.autocompletion;

import java.util.ArrayList;
import java.util.List;

public class FunctionsInformationLoader {

	private List<FunctionInformation> functionInformationList = new ArrayList<>();

	public void load() {

		// Create the function information for all available functions:
		functionInformationList.add(new FunctionInformation("GetRank",
			"Returns the id of the current node.", ""));

		functionInformationList.add(new FunctionInformation("GetSize",
			"Returns the total number of nodes.", ""));

		functionInformationList.add(new FunctionInformation("Barrier",
			"Parallel barrier, all nodes must reach the point of calling this function for any of them to continue further. Provides synchronization.",
			""));

		functionInformationList.add(new FunctionInformation("ReportProgress",
			"Outputs progress in percentage for a specified task in the node’s progress log.",
			"task, progress"));

		functionInformationList.add(new FunctionInformation("ReportText",
			"Outputs given text to the node’s log.", "text"));

		functionInformationList.add(new FunctionInformation("AddTask",
			"Creates a new task with the description provided.", "description"));

		functionInformationList.add(new FunctionInformation("ReportTasks",
			"Outputs all task ids with their descriptions.", ""));

		functionInformationList.add(new FunctionInformation("EnableTiming",
			"Enable timing of each tasks (from its start 0% to finish 100%) in the progress logs.",
			""));
	}

	public List<FunctionInformation> getFunctionInformationList() {
		return this.functionInformationList;
	}
}
