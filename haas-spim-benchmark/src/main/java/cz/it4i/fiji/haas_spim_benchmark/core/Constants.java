package cz.it4i.fiji.haas_spim_benchmark.core;

public interface Constants {
	long HAAS_UPDATE_TIMEOUT = 1000;
	String HAAS_JOB_NAME = "HaaSSPIMBenchmark";
	int HAAS_CLUSTER_NODE_TYPE = 6;
	int HAAS_TEMPLATE_ID = 4;
	String HAAS_PROJECT_ID = "DD-17-31";
	int HAAS_TIMEOUT = 9600;

	String SPIM_OUTPUT_FILENAME_PATTERN = "spim.outputFilenamePattern";
	String CONFIG_YAML = "config.yaml";
	String BENCHMARK_RESULT_FILE = "benchmark_result.csv";
	
	String STATISTICS_RESOURCES_MEMORY_USAGE = "resources_used.mem";
	String STATISTICS_RESOURCES_WALL_TIME = "resources_used.walltime";
	String STATISTICS_RESOURCES_CPU_PERCENTAGE = "resources_used.cpupercent";
}
