package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.LinkedList;

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
 		double memoryUsageTotal = 0; 
		for (ResultFileJob job : this.jobs ) {
			memoryUsageTotal += job.memoryUsed;
		}
		return (double)memoryUsageTotal / this.jobs.size();
	}
	
	public double getAverageWallTime() {
 		double wallTimeTotal = 0; 
		for (ResultFileJob job : this.jobs ) {
			wallTimeTotal += job.wallTime;
		}
		return (double)wallTimeTotal / this.jobs.size();
	}
	
	public double getAverageCpuPercentage() {
 		int cpuPercentageTotal = 0; 
		for (ResultFileJob job : this.jobs ) {
			cpuPercentageTotal += job.cpuPercentage;
		}
		return (double)cpuPercentageTotal / this.jobs.size();
	}
}