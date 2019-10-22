
package cz.it4i.fiji.hpc_workflow.ui;

import java.util.List;

public interface ProgressLogParser {

	public int getNumberOfNodes(List<String> progressLogs);
}
