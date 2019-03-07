
package cz.it4i.fiji.haas_spim_benchmark.core;

import static cz.it4i.fiji.haas_java_client.SynchronizableFileType.StandardErrorFile;
import static cz.it4i.fiji.haas_java_client.SynchronizableFileType.StandardOutputFile;
import static cz.it4i.fiji.haas_spim_benchmark.core.Configuration.getHaasUpdateTimeout;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.BENCHMARK_TASK_NAME_MAP;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cz.it4i.fiji.haas.HaaSOutputHolder;
import cz.it4i.fiji.haas.HaaSOutputHolderImpl;
import cz.it4i.fiji.haas.Job;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;

class SnakemakeOutputHelper implements HaaSOutputHolder {

	private final Job job;
	private final SPIMComputationAccessor computationAccessor;
	private final List<Task> tasks;
	private final List<BenchmarkError> nonTaskSpecificErrors;
	private int processedOutputLength;

	public SnakemakeOutputHelper(final Job job) {
		this.job = job;
		this.computationAccessor = createComputationAccessor();
		this.tasks = new ArrayList<>();
		this.nonTaskSpecificErrors = new ArrayList<>();
	}

	@Override
	public List<String> getActualOutput(
		final List<SynchronizableFileType> content)
	{
		return computationAccessor.getActualOutput(content);
	}

	synchronized List<Task> getTasks() {

		// If no tasks have been identified, try to search through the output
		if (tasks.isEmpty()) {
			resolveTasksAndNonTaskSpecificErrors();
		}

		// Should you have found some, process the output further
		if (!tasks.isEmpty()) {
			processOutput();
		}

		return tasks;
	}

	List<BenchmarkError> getErrors() {
		getTasks();
		final Stream<BenchmarkError> taskSpecificErrors = tasks.stream().flatMap(
			s -> s.getErrors().stream());
		return Stream.concat(nonTaskSpecificErrors.stream(), taskSpecificErrors)
			.collect(Collectors.toList());
	}

	private SPIMComputationAccessor createComputationAccessor() {
		SPIMComputationAccessor result = new SPIMComputationAccessor() {

			private final HaaSOutputHolder outputOfSnakemake =
				new HaaSOutputHolderImpl(list -> job.getOutput(list));

			@Override
			public List<String> getActualOutput(
				final List<SynchronizableFileType> content)
			{
				return outputOfSnakemake.getActualOutput(content);
			}

			@Override
			public java.util.Collection<String> getChangedFiles() {
				return job.getChangedFiles();
			}

			@Override
			public List<Long> getFileSizes(final List<String> names) {
				return job.getFileSizes(names);
			}

			@Override
			public List<String> getFileContents(final List<String> logs) {
				return job.getFileContents(logs);
			}
		};

		result = new SPIMComputationAccessorDecoratorWithTimeout(result,
			new HashSet<>(Arrays.asList(StandardOutputFile, StandardErrorFile)),
			getHaasUpdateTimeout() / UI_TO_HAAS_FREQUENCY_UPDATE_RATIO);
		return result;
	}

	private void resolveTasksAndNonTaskSpecificErrors() {

		final String OUTPUT_PARSING_JOB_COUNTS = "Job counts:";
		final String NOTHING_TO_BE_DONE = "Nothing to be done.";
		final String OUTPUT_PARSING_TAB_DELIMITER = "\\t";
		final int OUTPUT_PARSING_EXPECTED_NUMBER_OF_WORDS_PER_LINE = 2;
		final String OUTPUT_PARSING_WORKFLOW_ERROR = "WorkflowError";
		final String OUTPUT_PARSING_VALUE_ERROR = "ValueError";

		processedOutputLength = -1;
		boolean found = false;
		final String output = getSnakemakeOutput();

		// Found last job count definition
		final Pattern p = Pattern.compile(OUTPUT_PARSING_JOB_COUNTS + "|" +
			NOTHING_TO_BE_DONE);
		final Matcher m = p.matcher(output);
		while (m.find()) {
			processedOutputLength = m.start();
			found = true;
		}

		// If no job count definition has been found, search through the output and
		// list
		// all errors
		if (!found) {
			@SuppressWarnings("resource")
			final Scanner scanner = new Scanner(getSnakemakeOutput());
			String currentLine;
			while (scanner.hasNextLine()) {
				currentLine = scanner.nextLine().trim();
				if (currentLine.contains(OUTPUT_PARSING_WORKFLOW_ERROR) //
					|| currentLine.contains(OUTPUT_PARSING_VALUE_ERROR)) {
					String errorMessage = "";
					while (!currentLine.isEmpty()) {
						errorMessage += currentLine;
						if (!scanner.hasNextLine()) {
							break;
						}
						currentLine = scanner.nextLine().trim();
					}
					nonTaskSpecificErrors.add(new BenchmarkError(errorMessage));
				}
			}
			scanner.close();
			return;
		}

		// After the job count definition, task specification is expected
		@SuppressWarnings("resource")
		final Scanner scanner = new Scanner(output.substring(
			processedOutputLength));
		scanner.nextLine(); // Immediately after job count definition, task
												// specification table header is
		// expected
		while (scanner.hasNextLine()) {
			if (scanner.nextLine().trim().isEmpty()) {
				continue;
			}

			while (true) {
				final List<String> lineWords = Arrays.stream(scanner.nextLine().split(
					OUTPUT_PARSING_TAB_DELIMITER)).filter(word -> word.length() > 0)
					.collect(Collectors.toList());
				if (lineWords
					.size() != OUTPUT_PARSING_EXPECTED_NUMBER_OF_WORDS_PER_LINE)
				{
					break;
				}
				tasks.add(new Task(computationAccessor, lineWords.get(1), Integer
					.parseInt(lineWords.get(0))));
			}
			break;
		}
		scanner.close();

		// Order tasks chronologically
		if (!tasks.isEmpty()) {
			final List<String> chronologicList = BENCHMARK_TASK_NAME_MAP.keySet()
				.stream().collect(Collectors.toList());
			Collections.sort(tasks, Comparator.comparingInt(task -> chronologicList
				.indexOf(task.getDescription())));
		}
	}

	private void processOutput() {

		final String OUTPUT_PARSING_RULE = "rule ";
		final String OUTPUT_PARSING_COLON = ":";

		final String output = getSnakemakeOutput().substring(processedOutputLength);
		final int outputLengthToBeProcessed = output.length();

		int ruleRelativeIndex = -1;
		int colonRelativeIndex = -1;
		while (true) {

			ruleRelativeIndex = output.indexOf(OUTPUT_PARSING_RULE,
				colonRelativeIndex);
			colonRelativeIndex = output.indexOf(OUTPUT_PARSING_COLON,
				ruleRelativeIndex);

			if (ruleRelativeIndex == -1 || colonRelativeIndex == -1) {
				break;
			}

			final String taskDescription = output.substring(ruleRelativeIndex +
				OUTPUT_PARSING_RULE.length(), colonRelativeIndex);
			final List<Task> task = tasks.stream().filter(t -> t.getDescription()
				.equals(taskDescription)).collect(Collectors.toList());
			if (1 == task.size()) {
				// TODO: Consider throwing an exception
				task.get(0).populateTaskComputationParameters(processedOutputLength +
					ruleRelativeIndex);
			}
		}

		processedOutputLength = processedOutputLength + outputLengthToBeProcessed;
	}

	private String getSnakemakeOutput() {
		return computationAccessor.getActualOutput(Arrays.asList(
			SynchronizableFileType.StandardErrorFile)).get(0);
	}

}
