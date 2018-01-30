package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Collection;
import java.util.LinkedList;

public class Task {
	private SPIMComputationAccessor outputHolder;
	private String description;
	private Collection<TaskComputation> computations;
	private int numComputations;

	public Task(SPIMComputationAccessor outputHolder, String description, int numComputations) {
		this.description = description;
		this.outputHolder = outputHolder;
		this.numComputations = numComputations;
	}

	public Collection<TaskComputation> getComputations() {
		if (computations == null) {
			fillComputations();
		}
		return computations;
	}

	public String getDescription() {
		return description;
	}

	private void fillComputations() {
		computations = new LinkedList<>();
		for (int i = 0; i < numComputations; i++) {
			computations.add(new TaskComputation(outputHolder, this, i + 1));
		}
	}

}
