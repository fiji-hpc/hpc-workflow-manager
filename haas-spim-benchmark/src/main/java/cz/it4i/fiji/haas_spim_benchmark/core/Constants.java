package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.LinkedHashMap;
import java.util.Map;

public interface Constants {
	
	String MENU_ITEM_NAME = "Multiview Reconstruction";
	String SUBMENU_ITEM_NAME = "SPIM Workflow Manager for HPC";
	
	String PHONE = "123456789";
	int HAAS_UPDATE_TIMEOUT = 30000;
	short UI_TO_HAAS_FREQUENCY_UPDATE_RATIO = 10;
	String HAAS_JOB_NAME = "HaaSSPIMBenchmark";
	int HAAS_CLUSTER_NODE_TYPE = 7;
	int HAAS_TEMPLATE_ID = 4;
	String HAAS_PROJECT_ID = "";
	int HAAS_TIMEOUT = 3600; //Walltime in seconds
	long WAIT_FOR_SUBMISSION_TIMEOUT = 100;
	String BDS_ADDRESS = "http://julius2.it4i.cz/";
	
	final String NEW_LINE_SEPARATOR = "\n";
	final String DELIMITER = ";";
	final String FORWARD_SLASH = "/";

	String SPIM_OUTPUT_FILENAME_PATTERN = "spim.outputFilenamePattern";
	String VERIFIED_STATE_OF_FINISHED_JOB = "job.verifiedStateOfFinished";
	String CONFIG_YAML = "config.yaml";
	String HDF5_XML_FILENAME = "hdf5_xml_filename";
	String FUSION_SWITCH = "fusion_switch";
	
	
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
			put(DONE_TASK, "Done");
		}};
	
	String STATISTICS_TASK_NAME = "Task name";
	String STATISTICS_JOB_IDS = "job ids";
	String STATISTICS_JOB_COUNT = "jobs #";
	String STATISTICS_RESOURCES_MEMORY_USAGE = "resources_used.mem";
	String STATISTICS_RESOURCES_WALL_TIME = "resources_used.walltime";
	String STATISTICS_RESOURCES_CPU_PERCENTAGE = "resources_used.cpupercent";
	String STATISTICS_RESOURCES_START_TIME = "stime";
	
	String BENCHMARK_RESULT_FILE = "benchmark_result.csv";
	String STATISTICS_SUMMARY_FILENAME = "summary.csv";
	String SUMMARY_FILE_HEADER = "Task;AvgMemoryUsage;AvgWallTime;MaxWallTime;TotalTime;JobCount";
	String DONE_TASK = "done";
	int CORES_PER_NODE = 24;
	String DEMO_DATA_SIGNAL_FILE_NAME = "demodata";
	
	
}
