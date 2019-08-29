
package cz.it4i.fiji.haas_spim_benchmark.core;

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
			return -1;
		}
	}

	public void setProgress(int nodeId, long newProgress) {
		try {
			long oldProgress = this.progress.get(nodeId);
			if(newProgress > oldProgress) {
				this.progress.set(nodeId, newProgress);
			}
		}
		catch (Exception exc) {
			this.progress.add(nodeId, newProgress);
		}
	}
}