package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import com.google.common.collect.Streams;

import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;

public class TaskComputation {

	public static class Log {
		final private String name;
		final private String content;

		public Log(String name, String content) {
			this.name = name;
			this.content = content;
		}

		public String getName() {
			return name;
		}

		public String getContent() {
			return content;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Log other = (Log) obj;
			if (content == null) {
				if (other.content != null)
					return false;
			} else if (!content.equals(other.content))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}

	public static class File {
		final private String name;
		final private long size;

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

	private final SPIMComputationAccessor computationAccessor;
	private final String taskDescription;
	private final int timepoint;

	private JobState state;
	private int positionInOutput;

	private Collection<String> inputs;
	private Collection<String> outputs;
	private Collection<String> logs;
	private Long id;

	private final List<BenchmarkError> errors;

	/**
	 * Creates a TaskComputation object. At the time of creation, the job parameters
	 * are not populated
	 */
	public TaskComputation(SPIMComputationAccessor computationAccessor, String taskDescription, int timepoint) {
		this.computationAccessor = computationAccessor;
		this.taskDescription = taskDescription;
		this.timepoint = timepoint;
		this.errors = new LinkedList<BenchmarkError>();
		this.state = JobState.Unknown;
		updateState();
	}

	/**
	 * @return current job state
	 */
	public JobState getState() {
		updateState();
		return state;
	}

	/**
	 * @return job timepoint
	 */
	public int getTimepoint() {
		return timepoint;
	}

	/**
	 * @return job id
	 */
	public Long getId() {
		return id;
	}

	public void update() {

	}

	/**
	 * @return computations errors
	 */
	public Collection<BenchmarkError> getErrors() {
		return errors;
	}

	public Collection<Log> getLogs() {
		List<String> logNames = new LinkedList<>(logs);
		List<String> contents = computationAccessor.getFileContents(logNames);
		return Streams.<String, String, Log>zip(logNames.stream(), contents.stream(),
				(name, content) -> new Log(name, content)).collect(Collectors.toList());
	}

	/**
	 * Populates parameters of the current object by searching the output
	 * 
	 * @param positionInOutput:
	 *            Index of the output position to search from
	 * @return success flag
	 */
	public boolean populateParameters(int positionInOutput) {

		// Should the state be different than unknown, there is no need to populate
		// parameters
		if (state != JobState.Unknown) {
			return false;
		}

		this.positionInOutput = positionInOutput;
		if (!resolveJobParameters()) {
			return false;
		}

		state = JobState.Queued;
		updateState();

		return true;
	}

	public Collection<String> getOutputs() {
		return outputs;
	}

	public Map<String, Long> getOutFileSizes() {
		List<String> names = new LinkedList<>(outputs);
		List<Long> sizes = computationAccessor.getFileSizes(names);
		return Streams.zip(names.stream(), sizes.stream(), (name, size) -> new Pair<>(name, size))
				.collect(Collectors.toMap(p -> p.getFirst(), p -> p.getSecond()));
	}

	private void updateState() {

		// Should the state be queued, try to find out whether a log file exists
		if (state == JobState.Queued) {
			if (null != logs && !logs.stream().anyMatch(logFile -> computationAccessor.fileExists(logFile))) {
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

			Scanner scanner = new Scanner(getSnakemakeOutput().substring(positionInOutput));
			String currentLine;
			while (scanner.hasNextLine()) {
				currentLine = scanner.nextLine();
				if (currentLine.contains(desiredPatternErroneousJob)) {
					state = JobState.Failed;
					errors.add(new BenchmarkError(currentLine));
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

		return !(inputs == null || id == null);
	}

	private String getSnakemakeOutput() {
		return computationAccessor.getActualOutput(Arrays.asList(SynchronizableFileType.StandardErrorFile)).get(0);
	}

}
