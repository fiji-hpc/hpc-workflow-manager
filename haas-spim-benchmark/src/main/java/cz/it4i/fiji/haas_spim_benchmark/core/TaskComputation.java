package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Scanner;

import cz.it4i.fiji.haas_java_client.JobState;

public class TaskComputation {

	private final SPIMComputationAccessor outputHolder;
	private final Task task;
	private final int timepoint;
	private final Long id;
	
	//TASK 1011 what states will be defined and how it will be defined
	private JobState state = JobState.Unknown;
	
	private Collection<String> logs = new LinkedList<>();
	private Collection<String> outputs = new LinkedList<>();
	private Collection<String> inputs = new LinkedList<>();

	
	public TaskComputation(SPIMComputationAccessor outputHolder, Task task, int timepoint) {
		this.outputHolder = outputHolder;
		this.task = task;
		this.timepoint = timepoint;
		this.id = resolveId();
	}

	public JobState getState() {
		updateState();//TASK 1011 it is not good idea update every time when state is requested 
		return state;
	}

	private void updateState() {
		//TASK 1011 This should never happen, add some error handling to resolveId()
		if (id == null) {
			return;
		}
		
		String snakeOutput = outputHolder.getActualOutput();

		//TASK 1011 
		//resolve if job is queued (defined id), started (exists log file), finished (in log is Finished job 10.) or
		//or failed (some error in log)
	}

	private Long getId() {
		return id;
	}

	private Long resolveId() {
		
		final String OUTPUT_PARSING_RULE = "rule ";
		final String OUTPUT_PARSING_JOB_ID = "jobid: ";
		final String OUTPUT_PARSING_COLON = ":";
		final String desiredPattern = OUTPUT_PARSING_RULE + task.getDescription() + OUTPUT_PARSING_COLON;
		
		Scanner scanner = new Scanner(outputHolder.getActualOutput());
		int jobsToSkip = timepoint - 1;
		do {
			if (scanner.nextLine().equals(desiredPattern)) {
				jobsToSkip--;
			}
		} while (jobsToSkip >= 0 && scanner.hasNextLine());
		
		String currentLine;
		Long resolvedId = null;
		while (scanner.hasNextLine()) {
			currentLine = scanner.nextLine();
			if (!currentLine.contains(OUTPUT_PARSING_JOB_ID)) {
				continue;
			}
			resolvedId = Long.parseLong(currentLine.split(OUTPUT_PARSING_JOB_ID)[1]);
			break;
		}
		scanner.close();
		
		return resolvedId;
	}
	
}
