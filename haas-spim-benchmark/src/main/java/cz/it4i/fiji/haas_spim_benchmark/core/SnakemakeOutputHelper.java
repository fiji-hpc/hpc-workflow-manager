
package cz.it4i.fiji.haas_spim_benchmark.core;

import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.BENCHMARK_TASK_NAME_MAP;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cz.it4i.fiji.haas_java_client.SynchronizableFileType;

class SnakemakeOutputHelper {

	private final SPIMComputationAccessor computationAccessor;
	private final List<Task> tasks;
	private final List<BenchmarkError> nonTaskSpecificErrors;
	private int processedOutputLength;

	public SnakemakeOutputHelper(
		final SPIMComputationAccessor computationAccessor, final List<Task> tasks,
		final List<BenchmarkError> nonTaskSpecificErrors)
	{
		this.computationAccessor = computationAccessor;
		this.tasks = tasks;
		this.nonTaskSpecificErrors = nonTaskSpecificErrors;
	}

	List<Task> getTasks() {

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

	private void resolveTasksAndNonTaskSpecificErrors() {

		final String OUTPUT_PARSING_JOB_COUNTS = "Job counts:";
		final String OUTPUT_PARSING_TAB_DELIMITER = "\\t";
		final int OUTPUT_PARSING_EXPECTED_NUMBER_OF_WORDS_PER_LINE = 2;
		final String OUTPUT_PARSING_WORKFLOW_ERROR = "WorkflowError";
		final String OUTPUT_PARSING_VALUE_ERROR = "ValueError";

		processedOutputLength = -1;
		int readJobCountIndex = -1;
		boolean found = false;
		final String output = getSnakemakeOutput();

		// Found last job count definition
		while (true) {
			readJobCountIndex = output.indexOf(OUTPUT_PARSING_JOB_COUNTS,
				processedOutputLength + 1);

			if (readJobCountIndex < 0) {
				break;
			}

			found = true;
			processedOutputLength = readJobCountIndex;
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
