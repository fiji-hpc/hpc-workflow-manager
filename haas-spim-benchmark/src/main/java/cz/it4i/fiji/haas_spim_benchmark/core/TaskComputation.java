package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import cz.it4i.fiji.haas_java_client.JobState;

public class TaskComputation {

	private final SPIMComputationAccessor computationAccessor;
	private final Task task;
	private final int timepoint;
	
	private JobState state;
	private int positionInOutput;
	
	private Collection<String> inputs;
	private Collection<String> outputs;
	private Collection<String> logs;
	private Long id;
	
	public TaskComputation(SPIMComputationAccessor computationAccessor, Task task, int timepoint) {
		this.computationAccessor = computationAccessor;
		this.task = task;
		this.timepoint = timepoint;
		updateState();
	}

	public JobState getState() {
		updateState();
		return state;
	}
	
	public void update() {
	}

	public int getTimepoint() {
		return timepoint;
	}

	private void updateState() {

		// Should the state be undefined (null), try to look up job position in the computation output
		if (state == null) {
			positionInOutput = findPositionInOutput();
			if (0 > positionInOutput || !resolveJobParameters()) {
				return; // Job position has not been found or its parameters could not be resolved
			}
			state = JobState.Queued;
		}
		
		// Should the state be queued, try to find out whether a log file exists
		if (state == JobState.Queued ) {
			if (!logs.stream().anyMatch(logFile -> computationAccessor.fileExists(logFile))) {
				return; // No log file exists yet
			}
			state = JobState.Running;					
		}
		
		// Finally, look up any traces that the job has failed or finished
		if (state == JobState.Running) {
					
			final String OUTPUT_PARSING_FINISHED_JOB = "Finished job ";
			final String desiredPatternFinishedJob = OUTPUT_PARSING_FINISHED_JOB + id.toString();
			final String OUTPUT_PARSING_ERRONEOUS_JOB = "Error job ";
			final String desiredPatternErroneousJob = OUTPUT_PARSING_ERRONEOUS_JOB + id.toString();
			
			Scanner scanner = new Scanner(computationAccessor.getActualOutput().substring(positionInOutput));
			String currentLine;
			while (scanner.hasNextLine()) {
				currentLine = scanner.nextLine();
				if (currentLine.contains(desiredPatternErroneousJob)) {
					state = JobState.Failed;
					break;
				} else if (currentLine.contains(desiredPatternFinishedJob)) {
					state = JobState.Finished;
					break;
				}
			}
			scanner.close();
		}
	}
	
	private int findPositionInOutput() {
		
		final String OUTPUT_PARSING_RULE = "rule ";
		final String OUTPUT_PARSING_COLON = ":";
		final String desiredPattern = OUTPUT_PARSING_RULE + task.getDescription() + OUTPUT_PARSING_COLON;
		
		int taskComputationLineIndex = -1;
		for (int i = 0; i < timepoint; i++) {
			taskComputationLineIndex = computationAccessor.getActualOutput().indexOf(desiredPattern, taskComputationLineIndex);
		}
		
		return taskComputationLineIndex;
	}
	
	private boolean resolveJobParameters() {
		
		final String OUTPUT_PARSING_COMMA_SPACE = ", ";
		final String OUTPUT_PARSING_INPUTS = "input: ";
		final String OUTPUT_PARSING_OUTPUTS = "output: ";
		final String OUTPUT_PARSING_LOGS = "log: ";
		final String OUTPUT_PARSING_JOB_ID = "jobid: ";
		
		Scanner scanner = new Scanner(computationAccessor.getActualOutput().substring(positionInOutput));
		String currentLine;
		while (scanner.hasNextLine()) {
			currentLine = scanner.nextLine();
			if (currentLine.contains(OUTPUT_PARSING_INPUTS)) {
				inputs = Arrays.asList(currentLine.split(OUTPUT_PARSING_INPUTS)[1].split(OUTPUT_PARSING_COMMA_SPACE));
			} else if (currentLine.contains(OUTPUT_PARSING_OUTPUTS)) {
				outputs = Arrays.asList(currentLine.split(OUTPUT_PARSING_OUTPUTS)[1].split(OUTPUT_PARSING_COMMA_SPACE));
			} else if (currentLine.contains(OUTPUT_PARSING_LOGS)) {
				logs = Arrays.asList(currentLine.split(OUTPUT_PARSING_LOGS)[1].split(OUTPUT_PARSING_COMMA_SPACE));
			} else 
			if (currentLine.contains(OUTPUT_PARSING_JOB_ID)) {
				id = Long.parseLong(currentLine.split(OUTPUT_PARSING_JOB_ID)[1]);
			} else if (currentLine.trim().isEmpty()) {
				break;				
			}
		}
		scanner.close();
		
		return !(inputs == null || outputs == null || logs == null || id == null);
	}
	
}
