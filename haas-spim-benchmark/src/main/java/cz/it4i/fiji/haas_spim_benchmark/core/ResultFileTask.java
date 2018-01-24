package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class ResultFileTask {
	String name;
	LinkedList<ResultFileJob> jobs;

	public ResultFileTask(String name) {
		this.name = name;
		this.jobs = new LinkedList<ResultFileJob>();
	}

	public int getJobCount() {
		return this.jobs.size();
	}

	public double getAverageMemoryUsage() {
		return getAverage(Constants.STATISTICS_RESOURCES_MEMORY_USAGE);
	}

	public double getAverageWallTime() {
		return getAverage(Constants.STATISTICS_RESOURCES_WALL_TIME);
	}

	public double getAverageCpuPercentage() {
		return getAverage(Constants.STATISTICS_RESOURCES_CPU_PERCENTAGE);
	}
	
	private Double getAverage(String propertyName) {
		return jobs.stream().map(job -> job.getValue(propertyName))
				.map(memStr -> Double.parseDouble(memStr))
				.collect(Collectors.averagingDouble(d->d)).doubleValue();
	}
}