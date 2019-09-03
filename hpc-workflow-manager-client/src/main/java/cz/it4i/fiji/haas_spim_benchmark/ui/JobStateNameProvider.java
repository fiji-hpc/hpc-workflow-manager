package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import cz.it4i.fiji.haas_java_client.JobState;

class JobStateNameProvider {

	private final Map<JobState,String> translateTable = new EnumMap<>(JobState.class);
	
	public JobStateNameProvider() {
		translateTable.put(JobState.Configuring, "Configured");
	}
	
	public String getName(JobState state) {
		return Optional.ofNullable(translateTable.get(state)).orElseGet(state::toString);
	}
}
