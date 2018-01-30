package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Collection;
import java.util.LinkedList;

public class Task {
	private final String description;
	private final Collection<TaskComputation> computations;

	public Task(SPIMComputationAccessor outputHolder, String description, int numComputations) {
		this.description = description;
		this.computations = new LinkedList<>();
		
		for (int i = 0; i < numComputations; i++) {
			computations.add(new TaskComputation(outputHolder, this, i + 1));
		}
	}

	public String getDescription() {
		return description;
	}
	
	public Collection<TaskComputation> getComputations() {
		return computations;
	}
}
