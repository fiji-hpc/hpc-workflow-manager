
package cz.it4i.fiji.hpc_workflow.core;

import java.util.LinkedHashMap;
import java.util.Map;

public class Constants {

	private Constants() {
		// Private constructor to hide public.
	}

	public static final String MENU_ITEM_NAME = "Multiview Reconstruction";
	public static final String SUBMENU_ITEM_NAME =
		"HPC Workflow Manager";

	public static final String PHONE = "123456789";

	public static final short UI_TO_HAAS_FREQUENCY_UPDATE_RATIO = 10;
	public static final String HAAS_JOB_NAME = "HaaSSPIMBenchmark";
	public static final long WAIT_FOR_SUBMISSION_TIMEOUT = 100;

	public static final String NEW_LINE_SEPARATOR = "\n";
	public static final String DELIMITER = ";";
	public static final String FORWARD_SLASH = "/";

	public static final String SPIM_OUTPUT_FILENAME_PATTERN =
		"spim.outputFilenamePattern";
	public static final String VERIFIED_STATE_OF_FINISHED_JOB =
		"job.verifiedStateOfFinished";
	public static final String CONFIG_YAML = "config.yaml";
	public static final String HDF5_XML_FILENAME = "hdf5_xml_filename";
	public static final String FUSION_SWITCH = "fusion_switch";

	public static final String DONE_TASK = "done";

	// This map is considered as ground truth for chronological task sorting
	public static final Map<String, String> BENCHMARK_TASK_NAME_MAP =
		new LinkedHashMap<>();

	static {
		BENCHMARK_TASK_NAME_MAP.put("define_xml_czi", "Define dataset");
		BENCHMARK_TASK_NAME_MAP.put("define_xml_tif", "Define dataset");
		BENCHMARK_TASK_NAME_MAP.put("hdf5_xml", "Define hdf5 dataset");
		BENCHMARK_TASK_NAME_MAP.put("resave_hdf5", "Resave to hdf5");
		BENCHMARK_TASK_NAME_MAP.put("registration", "Detection and registration");
		BENCHMARK_TASK_NAME_MAP.put("xml_merge", "Merge xml");
		BENCHMARK_TASK_NAME_MAP.put("timelapse", "Time lapse registration");
		BENCHMARK_TASK_NAME_MAP.put("fusion", "Average fusion");
		BENCHMARK_TASK_NAME_MAP.put("external_transform",
			"External transformation");
		BENCHMARK_TASK_NAME_MAP.put("deconvolution", "Deconvolution GPU");
		BENCHMARK_TASK_NAME_MAP.put("define_output", "Define output");
		BENCHMARK_TASK_NAME_MAP.put("hdf5_xml_output", "Define hdf5 output");
		BENCHMARK_TASK_NAME_MAP.put("resave_hdf5_output", "Resave output to hdf5");
		BENCHMARK_TASK_NAME_MAP.put(DONE_TASK, "Done");
	}

	public static final String STATISTICS_TASK_NAME = "Task name";
	public static final String STATISTICS_JOB_IDS = "job ids";
	public static final String STATISTICS_JOB_COUNT = "jobs #";
	public static final String STATISTICS_RESOURCES_MEMORY_USAGE =
		"resources_used.mem";
	public static final String STATISTICS_RESOURCES_WALL_TIME =
		"resources_used.walltime";
	public static final String STATISTICS_RESOURCES_CPU_PERCENTAGE =
		"resources_used.cpupercent";
	public static final String STATISTICS_RESOURCES_START_TIME = "stime";
	public static final String BENCHMARK_RESULT_FILE = "benchmark_result.csv";
	public static final String DEFAULT_MACRO_FILE = "mpitest.txt";
	public static final String STATISTICS_SUMMARY_FILENAME = "summary.csv";
	public static final String SUMMARY_FILE_HEADER =
		"Task;AvgMemoryUsage;AvgWallTime;MaxWallTime;TotalTime;JobCount";
	public static final int CORES_PER_NODE = 24;
	public static final int NUMBER_OF_NODES = 2;
	public static final String DEMO_DATA_SIGNAL_FILE_NAME = "demodata";

}
