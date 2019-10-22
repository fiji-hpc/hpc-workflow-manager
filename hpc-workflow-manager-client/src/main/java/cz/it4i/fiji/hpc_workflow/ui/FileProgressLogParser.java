package cz.it4i.fiji.hpc_workflow.ui;

import java.util.List;

public class FileProgressLogParser implements ProgressLogParser{

	@Override
	public int getNumberOfNodes(List<String> progressLogs) {
		int numberOfNodes = 0;
		if (!progressLogs.isEmpty()) {
			String log = progressLogs.get(0);
			String[] lines = splitStringByDelimiter(log, "\n");
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
	
	private String[] splitStringByDelimiter(String stringToSplit,
		String delimiter)
	{
		return stringToSplit.split(delimiter);
	}

}
