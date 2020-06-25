
package cz.it4i.fiji.hpc_workflow.core;

import java.util.ArrayList;
import java.util.List;

public class MacroTask {

	private String description;

	// Progress in percentage for each node that the task run on:
	private List<Long> progress;

	public MacroTask(String description) {
		this.description = description;
		this.progress = new ArrayList<>();
	}
	
	public String getDescription() {
		return description;
	}

	public long getProgress(int nodeId) {
		try {
			return this.progress.get(nodeId);
		}
		catch (Exception e) {
			return -2;
		}
	}

	public void setProgress(int nodeId, long newProgress) {
		// Impossible percentages should not be allowed:
		if (newProgress > 100 || newProgress < 0) {
			return;
		}

		setProgressIncludingIndeterminate(nodeId, newProgress);
	}

	public void setIndeterminateProgress(int nodeId) {
		setProgressIncludingIndeterminate(nodeId, -1);
	}

	private void setProgressIncludingIndeterminate(int nodeId, long newProgress) {

		// Add new progress percentage if it is greater than the old one and the
		// node exists:
		try {
			long oldProgress = this.progress.get(nodeId);
			if (newProgress > oldProgress) {
				this.progress.set(nodeId, newProgress);
			}
		}
		catch (Exception exc) {
			// Add progress to specific index in array list, fill with null all
			// non-existing items that are before and including the index of the new
			// item:
			for (int i = this.progress.size(); i <= nodeId; i++) {
				this.progress.add(null);
			}
			this.progress.add(nodeId, newProgress);
		}
	}
}
