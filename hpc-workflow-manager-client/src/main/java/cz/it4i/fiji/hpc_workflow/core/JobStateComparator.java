package cz.it4i.fiji.hpc_workflow.core;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

import cz.it4i.fiji.hpc_client.JobState;

public class JobStateComparator implements Comparator<JobState> {

	private static Map<JobState, Integer> priorities = new EnumMap<>(JobState.class);

	private static void add(JobState state) {
		priorities.put(state, priorities.size());
	}

	static {
		Arrays.asList(JobState.Finished, JobState.Queued, JobState.Running, JobState.Canceled, JobState.Failed, JobState.Unknown)
				.forEach(JobStateComparator::add);
	}

	@Override
	public int compare(JobState o1, JobState o2) {
		if (!priorities.keySet().containsAll(Arrays.asList(o1, o2))) {
			throw new IllegalArgumentException("compare: " + o1 + ", " + o2);
		}
		return priorities.get(o1) - priorities.get(o2);
	}

}
