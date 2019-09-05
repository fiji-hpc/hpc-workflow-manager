package cz.it4i.fiji.haas_spim_benchmark.core;

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


public class TaskComputation {

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_spim_benchmark.core.TaskComputation.class);
	
	public static class Log {
		private final String name;
		private final String content;

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
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((content == null) ? 0 : content.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Log other = (Log) obj;
			if (content == null) {
				if (other.content != null) { return false;}
			}
			else if (!content.equals(other.content)) return false;
			if (name == null) {
				if (other.name != null) return false;
			}
			else if (!name.equals(other.name)) return false;
			return true;
		}
		
	}

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

	private final SPIMComputationAccessor computationAccessor;
	private final String taskDescription;
	private final int timepoint;

	private JobState state;
	private int positionInOutput;

	private Collection<String> inputs;
	private Collection<String> outputs = Collections.emptyList();
	private Collection<String> logs = Collections.emptyList();
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
		this.errors = new LinkedList<>();
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

	/**
	 * @return computations errors
	 */
	public Collection<BenchmarkError> getErrors() {
		return errors;
	}

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

	public Collection<String> getOutputs() {
		return outputs;
	}

	public Map<String, Long> getOutFileSizes() {
		List<String> names = new LinkedList<>(outputs);
		List<Long> sizes = computationAccessor.getFileSizes(names);
		return Streams.zip(names.stream(), sizes.stream(), Pair::new)
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
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
