package cz.it4i.fiji.hpc_workflow.core;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Streams;

public class ResultFileTask {
	private final String name;
	private final List<ResultFileJob> jobs;
	private DoubleSummaryStatistics memoryUsageStats;
	private DoubleSummaryStatistics startTimeStats;
	private DoubleSummaryStatistics wallTimeStats;
	private DoubleSummaryStatistics endTimeStats;
	private DoubleSummaryStatistics cpuPercentageStats;

	public ResultFileTask(String name) {
		this.name = name;
		this.jobs = new LinkedList<>();
	}

	public String getName() {
		return name;
	}

	public void setJobs(List<ResultFileJob> jobs) {

		this.jobs.clear();
		this.jobs.addAll(jobs);

		// Calculate start and wall time values
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.appendOptional(DateTimeFormatter.ofPattern("EEE MMM d kk:mm:ss z yyyy"))
				.appendOptional(DateTimeFormatter.ofPattern("EEE MMM d kk:mm:ss yyyy"))
				.appendOptional(DateTimeFormatter.ofPattern("EEE MMM  d kk:mm:ss yyyy")).toFormatter(Locale.ENGLISH);
		Collection<Double> startTimeValues = retrieveValues(Constants.STATISTICS_RESOURCES_START_TIME)
				.map(s -> s != null && !s.equals("null")
						? (double) LocalDateTime.parse(s, formatter).toEpochSecond(ZoneOffset.UTC)
						: Double.NaN)
				.collect(Collectors.toList());
		Collection<Double> wallTimeValues = retrieveValues(Constants.STATISTICS_RESOURCES_WALL_TIME)
				.map(Double::parseDouble).collect(Collectors.toList());

		// Calculate stats
		memoryUsageStats = retrieveValues(Constants.STATISTICS_RESOURCES_MEMORY_USAGE).map(Double::parseDouble)
				.collect(Collectors.summarizingDouble((Double::doubleValue)));
		startTimeStats = startTimeValues.stream().collect(Collectors.summarizingDouble((Double::doubleValue)));
		wallTimeStats = wallTimeValues.stream().collect(Collectors.summarizingDouble((Double::doubleValue)));
		endTimeStats = Streams.zip(startTimeValues.stream(), wallTimeValues.stream(), (stv, wtv) -> stv + wtv)
				.collect(Collectors.summarizingDouble((Double::doubleValue)));
		cpuPercentageStats = retrieveValues(Constants.STATISTICS_RESOURCES_CPU_PERCENTAGE)
				.map(Double::parseDouble).collect(Collectors.summarizingDouble((Double::doubleValue)));
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