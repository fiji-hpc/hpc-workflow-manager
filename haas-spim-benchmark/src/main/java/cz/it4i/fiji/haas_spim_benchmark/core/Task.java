package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.LinkedList;
import java.util.List;

public class Task {
	private final String description;
	private final List<TaskComputation> computations;

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
	
	public List<TaskComputation> getComputations() {
		return computations;
	}

	public void update() {
		// TODO Auto-generated method stub
		
	}
}
