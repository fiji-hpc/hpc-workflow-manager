
package cz.it4i.fiji.hpc_workflow.autocompletion;

import java.util.ArrayList;
import java.util.List;

public class FunctionsInformationLoader {

	private List<FunctionInformation> functionInformationList = new ArrayList<>();

	public void load() {

		// Create the function information for all available functions:
		functionInformationList.add(new FunctionInformation("Init",
			"Initializes parallelization, it should be called at the beginning of the parallel code.",
			""));

		functionInformationList.add(new FunctionInformation("Finalize",
			"Finalizes parallelization, it should be called at the end of the parallel code.",
			""));

		functionInformationList.add(new FunctionInformation("GetRank",
			"Returns the id of the current node.", ""));

		functionInformationList.add(new FunctionInformation("GetSize",
			"Returns the total number of nodes.", ""));

		functionInformationList.add(new FunctionInformation("Barrier",
			"Parallel barrier, all nodes must reach the point of calling this function for any of them to continue further. Provides synchronization.",
			""));

		functionInformationList.add(new FunctionInformation("ScatterEqually",
			"This will try to split an array to equal parts send it from the given rank. It will also receive the part of the array it should and return it (including the rank that sends the parts). In case the number of array elements is not equally divisible it will send any extra elements to the first rank (0).",
			"sendBuffer, totalSendBufferLength, root"));

		functionInformationList.add(new FunctionInformation("Scatter",
			"This works like parScatterEqually but in this case the user is responsible for providing the parameters to split the array.",
			"sendBuffer, sendCount, receiveCount, root"));
		
		functionInformationList.add(new FunctionInformation("ReportProgress",
			"Outputs progress in percentage for a specified task in the node’s progress log.",
			"task, progress"));
		
		functionInformationList.add(new FunctionInformation("ReportText",
			"Outputs given text to the node’s log.",
			"text"));
		
		functionInformationList.add(new FunctionInformation("AddTask",
			"Creates a new task with the description provided.",
			"description"));
		
		functionInformationList.add(new FunctionInformation("ReportTasks",
			"Outputs all task ids with their descriptions.",
			""));
	}

	public List<FunctionInformation> getFunctionInformationList() {
		return this.functionInformationList;
	}
}
