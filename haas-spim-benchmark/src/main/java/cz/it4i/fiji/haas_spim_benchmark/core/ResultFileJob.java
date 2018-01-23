package cz.it4i.fiji.haas_spim_benchmark.core;

public class ResultFileJob {
	String id;
	double memoryUsed;
	int wallTime;
	int cpuPercentage;
	public ResultFileJob(String id, double memoryUsed, int wallTime, int cpuPercentage) {
		this.id = id;
		this.memoryUsed = memoryUsed;
		this.wallTime = wallTime;
		this.cpuPercentage = cpuPercentage;
	}
}