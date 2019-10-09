package cz.it4i.fiji.hpc_workflow.core;

import com.google.common.collect.Streams;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.hpc_workflow.TaskComputation;


public class TaskComputationImpl implements TaskComputation {

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_workflow.core.TaskComputationImpl.class);
	

	public static class File {
		private final String name;
		private final long size;

		public File(String name, long size) {
			this.name = name;
			this.size = size;
		}

		public String getName() {
			return name;
		}

		public long getSize() {
			return size;
		}
	}

	private final ComputationAccessor computationAccessor;
	private final String taskDescription;
	private final int timepoint;

	private JobState state;
	private int positionInOutput;

	private Collection<String> inputs;
	private Collection<String> outputs = Collections.emptyList();
	private Collection<String> logs = Collections.emptyList();
	private Long id;

	private final List<HPCWorkflowError> errors;

	/**
	 * Creates a TaskComputation object. At the time of creation, the job parameters
	 * are not populated
	 */
	public TaskComputationImpl(ComputationAccessor computationAccessor,
		String taskDescription, int timepoint)
	{
		this.computationAccessor = computationAccessor;
		this.taskDescription = taskDescription;
		this.timepoint = timepoint;
		this.errors = new LinkedList<>();
		this.state = JobState.Unknown;
		updateState();
	}

	/**
	 * @return current job state
	 */
	@Override
	public JobState getState() {
		updateState();
		return state;
	}

	@Override
	/**
	 * @return job timepoint
	 */
	public int getTimepoint() {
		return timepoint;
	}

	@Override
	public Collection<Log> getLogs() {
		List<String> logNames = new LinkedList<>(logs);
		List<String> contents = computationAccessor.getFileContents(logNames);
		return Streams.<String, String, Log> zip(logNames.stream(), contents
			.stream(), Log::new).collect(Collectors.toList());
	}

	/**
	 * Populates parameters of the current object by searching the output
	 * 
	 * @param actualPositionInOutput
	 *            Index of the output position to search from
	 * @return success flag
	 */
	public boolean populateParameters(int actualPositionInOutput) {

		// Should the state be different than unknown, there is no need to populate
		// parameters
		if (state != JobState.Unknown) {
			return false;
		}

		this.positionInOutput = actualPositionInOutput;
		if (!resolveJobParameters()) {
			return false;
		}

		state = JobState.Queued;
		updateState();

		return true;
	}

	@Override
	public Collection<String> getOutputs() {
		return outputs;
	}

	@Override
	public Map<String, Long> getOutFileSizes() {
		List<String> names = new LinkedList<>(outputs);
		List<Long> sizes = computationAccessor.getFileSizes(names);
		return Streams.zip(names.stream(), sizes.stream(), Pair::new)
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	}

	/**
	 * @return computations errors
	 */
	Collection<HPCWorkflowError> getErrors() {
		return errors;
	}

	private void updateState() {

		// Should the state be queued, try to find out whether a log file exists
		if (state == JobState.Queued) {
			if (!taskDescription.equals(Constants.DONE_TASK) && null != logs
					&& logs.stream().noneMatch(computationAccessor::fileExists)) {
				return; // No log file exists yet
			}
			state = JobState.Running;
		}

		// Finally, look up any traces that the job has failed or finished
		if (state == JobState.Running) {

			final String OUTPUT_PARSING_FINISHED_JOB = "Finished job ";
			final String desiredPatternFinishedJob = OUTPUT_PARSING_FINISHED_JOB + id.toString();
			final String OUTPUT_PARSING_ERRONEOUS_JOB = "Error in job ";
			final String desiredPatternErroneousJob = OUTPUT_PARSING_ERRONEOUS_JOB + taskDescription;

			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(getSnakemakeOutput().substring(positionInOutput));
			String currentLine;
			while (scanner.hasNextLine()) {
				currentLine = scanner.nextLine();
				if (currentLine.contains(desiredPatternErroneousJob)) {
					state = JobState.Failed;
					errors.add(new HPCWorkflowError(currentLine));
					break;
				} else if (currentLine.contains(desiredPatternFinishedJob)) {
					state = JobState.Finished;
					break;
				}
			}
			scanner.close();
		}
	}

	private boolean resolveJobParameters() {

		final String OUTPUT_PARSING_COMMA_SPACE = ", ";
		final String OUTPUT_PARSING_INPUTS = "input: ";
		final String OUTPUT_PARSING_OUTPUTS = "output: ";
		final String OUTPUT_PARSING_LOGS = "log: ";
		final String OUTPUT_PARSING_JOB_ID = "jobid: ";

		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(getSnakemakeOutput().substring(positionInOutput));
		String currentLine;
		while (scanner.hasNextLine()) {
			currentLine = scanner.nextLine();
			if (currentLine.contains(OUTPUT_PARSING_INPUTS)) {
				inputs = Arrays.asList(currentLine.split(OUTPUT_PARSING_INPUTS)[1].split(OUTPUT_PARSING_COMMA_SPACE));
			} else if (currentLine.contains(OUTPUT_PARSING_OUTPUTS)) {
				outputs = Arrays.asList(currentLine.split(OUTPUT_PARSING_OUTPUTS)[1].split(OUTPUT_PARSING_COMMA_SPACE));
			} else if (currentLine.contains(OUTPUT_PARSING_LOGS)) {
				logs = Arrays.asList(currentLine.split(OUTPUT_PARSING_LOGS)[1].split(OUTPUT_PARSING_COMMA_SPACE));
			} else if (currentLine.contains(OUTPUT_PARSING_JOB_ID)) {
				id = Long.parseLong(currentLine.split(OUTPUT_PARSING_JOB_ID)[1]);
			} else if (currentLine.trim().isEmpty()) {
				break;
			}
		}
		scanner.close();
		if (log.isDebugEnabled()) {
			log.debug("Job parameters resolved id = {}, inputs = {}", id, inputs);
		}
		return id != null;
	}

	private String getSnakemakeOutput() {
		return computationAccessor.getActualOutput(Arrays.asList(SynchronizableFileType.StandardErrorFile)).get(0);
	}

}
