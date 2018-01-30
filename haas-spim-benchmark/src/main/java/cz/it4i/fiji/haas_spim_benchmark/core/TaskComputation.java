package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Collection;
import java.util.LinkedList;

import cz.it4i.fiji.haas_java_client.JobState;

public class TaskComputation {

	private final SPIMComputationAccessor outputHolder;
	private final Task task;
	private final int timepoint;
	
	private Long id;
	//TASK 1011 what states will be defined and how it will be defined
	private JobState state = JobState.Unknown;
	private Collection<String> logs = new LinkedList<>();
	private Collection<String> outputs = new LinkedList<>();
	private Collection<String> inputs = new LinkedList<>();

	
	public TaskComputation(SPIMComputationAccessor outputHolder, Task task, int timepoint) {
		this.outputHolder = outputHolder;
		this.task = task;
		this.timepoint = timepoint;
	}

	public JobState getState() {
		updateState();//TASK 1011 it is not good idea update every time when state is requested 
		return state;
	}

	private void updateState() {
		String snakeOutput = outputHolder.getActualOutput();
		Long id = getId();
		if(id == null) {
			return;
		}
		//TASK 1011 
		//resolve if job is queued (defined id), started (exists log file), finished (in log is Finished job 10.) or
		//or failed (some error in log)
	}

	private Long getId() {
		if (id == null) {
			fillId();
		}
		return id;
	}

	private void fillId() {
		//TASK 1011 
		//find timepoint-th occurence of
//rule resave_hdf5:
//    input: HisRFP_test-01-00.h5_xml, HisRFP_test_first.xml
//    output: HisRFP_test-01-00.h5, HisRFP_test-01-00.h5_hdf5
//    log: logs/b2_resave_hdf5-01.log
//    jobid: 7
//    wildcards: xml_base=HisRFP_test, file_id=01
		
	//resave_hdf5 == task.getDescription()
	//jobid -> id
	//input->inputs
	//...
	//
	//or return
	}
	
}
