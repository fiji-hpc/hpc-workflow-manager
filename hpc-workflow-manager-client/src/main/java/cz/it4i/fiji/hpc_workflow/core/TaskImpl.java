package cz.it4i.fiji.hpc_workflow.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.hpc_workflow.Task;
import cz.it4i.fiji.hpc_workflow.TaskComputation;

class TaskImpl implements Task {

	private final String description;
	private final List<TaskComputationImpl> computations;

	/**
	 * Creates a Task object with given description and expected number of
	 * computations within.
	 */
	public TaskImpl(ComputationAccessor computationAccessor, String description, int numOfExpectedComputations) {
		this.description = description;
		this.computations = new LinkedList<>();
		for (int i = 0; i < numOfExpectedComputations; i++) {
			computations.add(new TaskComputationImpl(computationAccessor, description,
				i + 1));
		}
	}

	/**
	 * Looks up next task computation (i.e. with lowest timepoint) of the current
	 * task and populates its parameters
	 * 
	 * @param positionInOutput
	 *            Index of the output position to search from
	 * @return success flag
	 */
	boolean populateTaskComputationParameters(int positionInOutput) {
		Optional<TaskComputationImpl> otc = computations.stream().filter(c -> c
			.getState().equals(JobState.Unknown)).min(Comparator.comparingInt(
				TaskComputationImpl::getTimepoint));
		TaskComputationImpl tc = null;
		if (otc.isPresent()) {
			tc = otc.get();
		}
		if (tc == null) {
			return false;
		}
		return tc.populateParameters(positionInOutput);
	}

	/**
	 * @return task description
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @return list of task computations
	 */
	@Override
	public List<TaskComputation> getComputations() {
		return Collections.unmodifiableList(computations);
	}
	
	/**
	 * @return all errors corresponding to this task
	 */
	@Override
	public List<HPCWorkflowError> getErrors() {
		return computations.stream().flatMap(s -> s.getErrors().stream()).collect(Collectors.toList());
	}

}
