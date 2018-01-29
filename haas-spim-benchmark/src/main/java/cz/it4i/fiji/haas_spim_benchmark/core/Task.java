package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Collection;

import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.Job;

public class Task {
	private String description;
	private Job job;
	private Collection<TaskComputation> computations;
	
	public Task(Job job,String description) {
		this.description = description;
		this.job = job;
	}

	public String getDescription() {
		return description;
	}
	
	public Collection<Task> getPredecessors() {
		return null;
	}
}
