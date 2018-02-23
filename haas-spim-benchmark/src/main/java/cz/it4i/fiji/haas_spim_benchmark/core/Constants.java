package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.LinkedHashMap;
import java.util.Map;

public interface Constants {
	String PHONE = "123456789";
	int HAAS_UPDATE_TIMEOUT = 30000;
	short UI_TO_HAAS_FREQUENCY_UPDATE_RATIO = 10;
	String HAAS_JOB_NAME = "HaaSSPIMBenchmark";
	int HAAS_CLUSTER_NODE_TYPE = 6;
	int HAAS_TEMPLATE_ID = 4;
	String HAAS_PROJECT_ID = "DD-17-31";
	int HAAS_TIMEOUT = 9600; //Walltime in seconds
	
	final String NEW_LINE_SEPARATOR = "\n";
	final String DELIMITER = ";";
	final String FORWARD_SLASH = "/";

	String SPIM_OUTPUT_FILENAME_PATTERN = "spim.outputFilenamePattern";
	String CONFIG_YAML = "config.yaml";
	String BENCHMARK_RESULT_FILE = "benchmark_result.csv";
	
	// This map is considered as ground truth for chronologic task sorting
	Map<String, String> BENCHMARK_TASK_NAME_MAP = new LinkedHashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("define_xml_czi", "Define dataset");
			put("define_xml_tif", "Define dataset");
			put("hdf5_xml", "Define hdf5 dataset");
			put("resave_hdf5", "Resave to hdf5");
			put("registration", "Detection and registration");
			put("xml_merge", "Merge xml");
			put("timelapse", "Time lapse registration");
			put("fusion", "Average fusion");
			put("external_transform", "External transformation");
			put("deconvolution", "Deconvolution GPU");
			put("define_output", "Define output");
			put("hdf5_xml_output", "Define hdf5 output");
			put("resave_hdf5_output", "Resave output to hdf5");
			put("done", "Done");
		}};
	
	String STATISTICS_TASK_NAME = "Task name";
	String STATISTICS_JOB_IDS = "job ids";
	String STATISTICS_JOB_COUNT = "jobs #";
	String STATISTICS_RESOURCES_MEMORY_USAGE = "resources_used.mem";
	String STATISTICS_RESOURCES_WALL_TIME = "resources_used.walltime";
	String STATISTICS_RESOURCES_CPU_PERCENTAGE = "resources_used.cpupercent";
	String STATISTICS_RESOURCES_START_TIME = "stime";
	
	String STATISTICS_SUMMARY_FILENAME = "summary.csv";
	String SUMMARY_FILE_HEADER = "Task;AvgMemoryUsage;AvgWallTime;MaxWallTime;TotalTime;JobCount";
	
}
