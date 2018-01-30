package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.LinkedList;
import java.util.List;

public class Task {
	private SPIMComputationAccessor outputHolder;
	private String description;
	private List<TaskComputation> computations;
	private int numComputations;

	public Task(SPIMComputationAccessor outputHolder, String description, int numComputations) {
		this.description = description;
		this.outputHolder = outputHolder;
		this.numComputations = numComputations;
	}

	public List<TaskComputation> getComputations() {
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

	public void update() {
		// TODO Auto-generated method stub
		
	}

}
