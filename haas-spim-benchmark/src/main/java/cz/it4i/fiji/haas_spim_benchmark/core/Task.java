package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import cz.it4i.fiji.haas_java_client.JobState;

public class Task {
	
	private final String description;
	private final List<TaskComputation> computations;

	public Task(SPIMComputationAccessor computationAccessor, String description, int numOfExpectedComputations) {
		this.description = description;
		this.computations = new LinkedList<>();		
		for (int i = 0; i < numOfExpectedComputations; i++) {
			computations.add(new TaskComputation(computationAccessor, i + 1));
		}
	}
	
	/**
	 * Looks up next task computation (i.e. with lowest timepoint) of the current task and populates its parameters
	 * @param positionInOutput: Index of the output position to search from
	 * @return success flag
	 */
	public boolean populateTaskComputationParameters(int positionInOutput) {
		TaskComputation tc = computations.stream()
				.filter(c -> c.getState().equals(JobState.Unknown))
				.min(Comparator.comparingInt(c -> c.getTimepoint()))
				.get();
		if (null == tc) {
			return false;
		}
		return tc.populateParameters(positionInOutput);
	}

	/**
	 * @return task description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @return list of task computations
	 */
	public List<TaskComputation> getComputations() {
		return computations;
	}
	
	// TODO: Method stub
	public void update() {

	}
	
}
