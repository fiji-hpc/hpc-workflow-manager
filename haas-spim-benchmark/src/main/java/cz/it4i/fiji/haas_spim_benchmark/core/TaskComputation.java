package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Scanner;

import cz.it4i.fiji.haas_java_client.JobState;

public class TaskComputation {
	
	// A single-purpose class dedicated to help with TaskComputation members initialization
	private class ParsedTaskComputationValues {
		private final Collection<String> logs;
		private final Collection<String> inputs;
		private final Collection<String> outputs;
		private final Long id;
		
		public ParsedTaskComputationValues(Collection<String> inputs, Collection<String> outputs, Collection<String> logs, Long id) {
			this.inputs = inputs;
			this.outputs = outputs;
			this.logs = logs;
			this.id = id;
		}
	}

	private final SPIMComputationAccessor computationAccessor;
	private final Task task;
	private final int timepoint;
	private final Collection<String> inputs;
	private final Collection<String> outputs;
	private final Collection<String> logs;
	private final Long id;
	
	//TASK 1011 what states will be defined and how it will be defined
	private JobState state = JobState.Unknown;
	
	public TaskComputation(SPIMComputationAccessor computationAccessor, Task task, int timepoint) {
		this.computationAccessor = computationAccessor;
		this.task = task;
		this.timepoint = timepoint;
		ParsedTaskComputationValues parsedValues = parseStuff(computationAccessor);
		this.inputs = parsedValues.inputs;
		this.outputs = parsedValues.outputs;
		this.logs = parsedValues.logs;
		this.id = parsedValues.id;
		updateState();
	}

	public JobState getState() {
		updateState();//TASK 1011 it is not good idea update every time when state is requested 
		return state;
	}
	
	public void update() {
	}

	public int getTimepoint() {
		return timepoint;
	}

	private void updateState() {
		//TASK 1011 This should never happen, add some error handling to resolveId()
		if (id == null) {
			state = JobState.Unknown;
			return;
		}
		
		state = JobState.Queued;
		
		// Check whether a log file exists
		if (!logs.stream().anyMatch(logFile -> computationAccessor.fileExists(logFile))) {
			return;
		}
		
		state = JobState.Running;
		
		// Check whether the corresponding job has finished
		final String OUTPUT_PARSING_FINISHED_JOB = "Finished job ";
		final String desiredPatternFinishedJob = OUTPUT_PARSING_FINISHED_JOB + id.toString();
		final String OUTPUT_PARSING_ERRONEOUS_JOB = "Error job ";
		final String desiredPatternErroneousJob = OUTPUT_PARSING_ERRONEOUS_JOB + id.toString();
		String currentLine;
		Scanner scanner = new Scanner(computationAccessor.getActualOutput());
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
		
		return;
	}

	private Long getId() {
		return id;
	}

	private ParsedTaskComputationValues parseStuff(SPIMComputationAccessor outputHolder) {
		
		final String OUTPUT_PARSING_RULE = "rule ";
		final String OUTPUT_PARSING_COLON = ":";
		final String OUTPUT_PARSING_COMMA_SPACE = ", ";
		final String desiredPattern = OUTPUT_PARSING_RULE + task.getDescription() + OUTPUT_PARSING_COLON;
		
		final String OUTPUT_PARSING_INPUTS = "input: ";
		final String OUTPUT_PARSING_OUTPUTS = "output: ";
		final String OUTPUT_PARSING_LOGS = "log: ";
		final String OUTPUT_PARSING_JOB_ID = "jobid: ";
		
		Scanner scanner = new Scanner(outputHolder.getActualOutput());
		int jobsToSkip = timepoint;
		while (scanner.hasNextLine() && jobsToSkip > 0) {
			if (scanner.nextLine().equals(desiredPattern)) {
				jobsToSkip--;
			}
		}
		
		String currentLine;
		Collection<String> resolvedInputs = new LinkedList<>();
		Collection<String> resolvedOutputs = new LinkedList<>();
		Collection<String> resolvedLogs = new LinkedList<>();
		Long resolvedId = null;
		while (scanner.hasNextLine()) {
			currentLine = scanner.nextLine();
			if (currentLine.contains(OUTPUT_PARSING_INPUTS)) {
				resolvedInputs = Arrays.asList(currentLine.split(OUTPUT_PARSING_INPUTS)[1].split(OUTPUT_PARSING_COMMA_SPACE));
			} else if (currentLine.contains(OUTPUT_PARSING_OUTPUTS)) {
				resolvedOutputs = Arrays.asList(currentLine.split(OUTPUT_PARSING_OUTPUTS)[1].split(OUTPUT_PARSING_COMMA_SPACE));
			} else if (currentLine.contains(OUTPUT_PARSING_LOGS)) {
				resolvedLogs = Arrays.asList(currentLine.split(OUTPUT_PARSING_LOGS)[1].split(OUTPUT_PARSING_COMMA_SPACE));
			} else if (currentLine.contains(OUTPUT_PARSING_JOB_ID)) {
				resolvedId = Long.parseLong(currentLine.split(OUTPUT_PARSING_JOB_ID)[1]);
			} else if (currentLine.trim().isEmpty()) {
				break;				
			}
		}
		scanner.close();
		
		return new ParsedTaskComputationValues(resolvedInputs, resolvedOutputs, resolvedLogs, resolvedId);
	}	
}
