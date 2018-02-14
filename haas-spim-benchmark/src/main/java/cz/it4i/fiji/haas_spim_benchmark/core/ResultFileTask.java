package cz.it4i.fiji.haas_spim_benchmark.core;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Streams;

public class ResultFileTask {
	private String name;
	private List<ResultFileJob> jobs;
	private DoubleSummaryStatistics memoryUsageStats;
	private DoubleSummaryStatistics startTimeStats;
	private DoubleSummaryStatistics wallTimeStats;
	private DoubleSummaryStatistics endTimeStats;
	private DoubleSummaryStatistics cpuPercentageStats;

	public ResultFileTask(String name) {
		this.name = name;
		this.jobs = new LinkedList<ResultFileJob>();
	}

	public String getName() {
		return name;
	}

	public void setJobs(List<ResultFileJob> jobs) {

		this.jobs.addAll(jobs);

		// Calculate start and wall time values
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.appendOptional(DateTimeFormatter.ofPattern("EEE MMM dd kk:mm:ss z yyyy"))
				.appendOptional(DateTimeFormatter.ofPattern("EEE MMM dd kk:mm:ss yyyy")).toFormatter();
		Collection<Double> startTimeValues = retrieveValues(Constants.STATISTICS_RESOURCES_START_TIME)
				.map(s -> (double) LocalDateTime.parse(s, formatter).toEpochSecond(ZoneOffset.UTC))
				.collect(Collectors.toList());
		Collection<Double> wallTimeValues = retrieveValues(Constants.STATISTICS_RESOURCES_WALL_TIME)
				.map(s -> Double.parseDouble(s)).collect(Collectors.toList());

		// Calculate stats
		memoryUsageStats = retrieveValues(Constants.STATISTICS_RESOURCES_MEMORY_USAGE).map(s -> Double.parseDouble(s))
				.collect(Collectors.summarizingDouble((Double::doubleValue)));
		startTimeStats = startTimeValues.stream().collect(Collectors.summarizingDouble((Double::doubleValue)));
		wallTimeStats = wallTimeValues.stream().collect(Collectors.summarizingDouble((Double::doubleValue)));
		endTimeStats = Streams.zip(startTimeValues.stream(), wallTimeValues.stream(), (stv, wtv) -> stv + wtv)
				.collect(Collectors.summarizingDouble((Double::doubleValue)));
		cpuPercentageStats = retrieveValues(Constants.STATISTICS_RESOURCES_CPU_PERCENTAGE)
				.map(s -> Double.parseDouble(s)).collect(Collectors.summarizingDouble((Double::doubleValue)));
	}

	public int getJobCount() {
		return this.jobs.size();
	}

	public double getAverageMemoryUsage() {
		return memoryUsageStats.getAverage();
	}

	public double getAverageWallTime() {
		return wallTimeStats.getAverage();
	}

	public double getMaximumWallTime() {
		return wallTimeStats.getMax();
	}

	public double getEarliestStartInSeconds() {
		return startTimeStats.getMin();
	}

	public double getLatestEndInSeconds() {
		return endTimeStats.getMax();
	}

	public double getTotalTime() {
		return getLatestEndInSeconds() - getEarliestStartInSeconds();
	}

	public double getAverageCpuPercentage() {
		return cpuPercentageStats.getAverage();
	}

	private Stream<String> retrieveValues(String propertyName) {
		return jobs.stream().map(job -> job.getValue(propertyName));
	}
}