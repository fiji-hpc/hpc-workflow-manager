
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.ArrayList;
import java.util.List;

public class MPITask {

	private String description;

	// Progress in percentage for each node that the task run on:
	private List<Long> progress;

	public MPITask(String description) {
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

	public void setProgress(int nodeId, Long newProgress) {
		try {
			this.progress.get(nodeId);
			this.progress.set(nodeId, newProgress);
		}
		catch (Exception exc) {
			this.progress.add(nodeId, newProgress);
		}
	}
}
