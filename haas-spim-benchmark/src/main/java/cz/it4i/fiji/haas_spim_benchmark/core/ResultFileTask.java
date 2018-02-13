package cz.it4i.fiji.haas_spim_benchmark.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.Streams;

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
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.appendOptional(DateTimeFormatter.ofPattern("EEE MMM dd kk:mm:ss z yyyy"))
				.appendOptional(DateTimeFormatter.ofPattern("EEE MMM dd kk:mm:ss yyyy")).toFormatter();
		Collection<Double> startTimeValues = retrieveValues(Constants.STATISTICS_RESOURCES_START_TIME)
				.map(s -> (double) LocalDateTime.parse(s, formatter).getSecond()).collect(Collectors.toList());
		Stream<Double> wallTimeValues = retrieveValues(Constants.STATISTICS_RESOURCES_WALL_TIME)
				.map(s -> Double.parseDouble(s));
		Stream<Double> endTimeValues = Streams.zip(startTimeValues.stream(), wallTimeValues, (stv, wtv) -> stv + wtv);
		return endTimeValues.mapToDouble(s -> s).max().getAsDouble()
				- startTimeValues.stream().mapToDouble(s -> s).min().getAsDouble();
	}

	public double getAverageCpuPercentage() {
		return getAverage(Constants.STATISTICS_RESOURCES_CPU_PERCENTAGE);
	}

	private Double getAverage(String propertyName) {
		return retrieveValues(propertyName).mapToDouble(s -> Double.parseDouble(s)).average().getAsDouble();
	}

	private Double getMaximum(String propertyName) {
		return retrieveValues(propertyName).mapToDouble(s -> Double.parseDouble(s)).max().getAsDouble();
	}

	private Stream<String> retrieveValues(String propertyName) {
		return jobs.stream().map(job -> job.getValue(propertyName));
	}
}