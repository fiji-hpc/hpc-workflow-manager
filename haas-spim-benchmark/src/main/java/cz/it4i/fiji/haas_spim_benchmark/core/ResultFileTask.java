package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.LinkedList;
import java.util.stream.DoubleStream;

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

	public double getMaximumWallTime() {
		return getMaximum(Constants.STATISTICS_RESOURCES_WALL_TIME);
	}

	public double getTotalTime() {
		DoubleStream startTimeValues = retrieveValuesAsDouble(Constants.STATISTICS_RESOURCES_START_TIME);
		DoubleStream wallTimeValues = retrieveValuesAsDouble(Constants.STATISTICS_RESOURCES_WALL_TIME);
		DoubleStream endTimeValues = startTimeValues.flatMap(st -> wallTimeValues.map(wt -> st + wt));
		return endTimeValues.max().getAsDouble() - startTimeValues.min().getAsDouble();
	}

	public double getAverageCpuPercentage() {
		return getAverage(Constants.STATISTICS_RESOURCES_CPU_PERCENTAGE);
	}
	
	private Double getAverage(String propertyName) {
		return retrieveValuesAsDouble(propertyName).average().getAsDouble();
	}

	private Double getMinimum(String propertyName) {
		return retrieveValuesAsDouble(propertyName).min().getAsDouble();
	}

	private Double getMaximum(String propertyName) {
		return retrieveValuesAsDouble(propertyName).max().getAsDouble();
	}

	private DoubleStream retrieveValuesAsDouble(String propertyName) {
		return jobs.stream().mapToDouble(job -> Double.parseDouble(job.getValue(propertyName)));
	}
}