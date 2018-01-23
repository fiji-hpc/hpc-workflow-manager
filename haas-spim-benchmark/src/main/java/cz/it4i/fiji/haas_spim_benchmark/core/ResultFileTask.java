package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.LinkedList;
import java.util.function.Function;
import java.util.stream.Collector;
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
		return getAverage(str->Double.parseDouble(str), Collectors.averagingDouble(d->d),"resources_used.mem").doubleValue();
	}

	public double getAverageWallTime() {
		return getAverage("resources_used.walltime");
	}

	public double getAverageCpuPercentage() {
		return getAverage("resources_used.cpupercent");
	}

	private double getAverage(String propertyName) {
		return getAverage(str->Integer.parseInt(str), Collectors.averagingInt(i->i),propertyName).doubleValue();
	}
	
	private<T> Double getAverage(Function<String, T> valueProvider,Collector<T,?,Double> collector,String propertyName) {
		return jobs.stream().map(job -> job.getValue(propertyName)).map(memStr -> valueProvider.apply(memStr))
				.collect(collector);
	}
}