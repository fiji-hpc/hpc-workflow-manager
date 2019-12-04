
package cz.it4i.fiji.hpc_workflow.ui;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.hpc_adapter.ui.TableCellAdapter;
import cz.it4i.fiji.hpc_adapter.ui.TableViewContextMenu;
import cz.it4i.fiji.hpc_client.JobState;
import cz.it4i.fiji.hpc_workflow.Task;
import cz.it4i.fiji.hpc_workflow.TaskComputation;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.fiji.hpc_workflow.core.FXFrameExecutorService;
import cz.it4i.fiji.hpc_workflow.core.SimpleObservableList;
import cz.it4i.fiji.hpc_workflow.core.SimpleObservableValue;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SPIMPipelineProgressViewController extends BorderPane
{

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_workflow.ui.SPIMPipelineProgressViewController.class);

	private static final String EMPTY_VALUE = "";

	private static final int PREFERRED_WIDTH = 900;

	private static final Map<JobState, Color> taskExecutionState2Color =
		new EnumMap<>(JobState.class);

	private static final double TIMEPOINT_TABLE_COLUMN_WIDTH_RATIO = 6;
	static {
		taskExecutionState2Color.put(JobState.Running, Color.rgb(0xF2, 0xD5, 0x39));
		taskExecutionState2Color.put(JobState.Finished, Color.rgb(0x41, 0xB2,
			0x80));
		taskExecutionState2Color.put(JobState.Failed, Color.rgb(0xFF, 0x51, 0x3D));
		taskExecutionState2Color.put(JobState.Queued, Color.rgb(0x30, 0xA2, 0xCC));
		taskExecutionState2Color.put(JobState.Unknown, Color.GRAY);
	}

	private static String getColorTaskExecState(JobState jobState) {
		Color result = null;
		if (jobState == null) {
			result = Color.GRAY;
		}
		else {
			result = taskExecutionState2Color.get(jobState);
		}
		return JavaFXRoutines.toCss(result != null ? result : Color.ORANGE);
	}

	@FXML
	private TableView<Task> tasks;

	private SimpleObservableList<Task> observedList;

	private final Executor executorFx = new FXFrameExecutorService();
	private Stage root;

	private boolean filled = false;
	private boolean closed;

	private final ListChangeListener<Task> taskChangeListener = (
		Change<? extends Task> c) -> {
			// We are assuming that once the table has been filled,
			// it cannot be "unfilled", i.e. the tasks cannot be
			// changed or removed at runtime.
			if (!filled)
			{
			fillTable();
		}
		else {
			tasks.refresh();
		}
		};

	public SPIMPipelineProgressViewController() {
		init();
	}

	public synchronized void close() {
		observedList.unsubscribe(taskChangeListener);
		closed = true;
	}

	public void init(Stage parameter) {
		this.root = parameter;
	}

	public void setObservable(final SimpleObservableList<Task> taskList) {
		observedList = taskList;
		observedList.subscribe(taskChangeListener);
	}

	void init() {
		JavaFXRoutines.initRootAndController("SPIMPipelineProgressView.fxml", this);
		tasks.setPrefWidth(PREFERRED_WIDTH);

		TableViewContextMenu<Task> menu = new TableViewContextMenu<>(this.tasks);
		menu.addItem("Open view", this::proof, this::check);
	}

	private boolean check(Task x, Integer columnIndex) {
		return (x != null && 0 < columnIndex && columnIndex - 1 < x
			.getComputations().size());
	}

	private void proof(Task task, int columnIndex) {
		new TaskComputationWindow(root, task.getComputations().get(columnIndex -
			1));
	}

	private synchronized void fillTable() {
		if (closed || filled) {
			return;
		}

		final Optional<List<TaskComputation>> optional = getComputations(
			observedList);

		if (!optional.isPresent() || optional.get().isEmpty()) {
			return;
		}

		final List<TaskComputation> computations = optional.get();

		executorFx.execute(() -> {
			int i = 0;
			JavaFXRoutines.setCellValueFactoryForList(this.tasks, i++,
				f -> new SimpleObservableValue<>(Constants.BENCHMARK_TASK_NAME_MAP
					.getOrDefault(f.getValue().getDescription(), f.getValue()
						.getDescription())));

			final double tableColumnWidth = computeTableColumnWidth(computations);
			for (TaskComputation tc : computations) {
				TableColumn<Task, String> tableCol;
				tableCol = new TableColumn<>(columnHeader(tc));
				this.tasks.getColumns().add(tableCol);
				int index = i++;
				tableCol.setPrefWidth(tableColumnWidth);
				constructCellFactory(index);
			}
			this.tasks.setItems(observedList);
		});

		filled = true;
	}

	private long computeTableColumnWidth(List<TaskComputation> computations) {
		return Math.round(this.tasks.getColumns().get(0).getWidth() /
			TIMEPOINT_TABLE_COLUMN_WIDTH_RATIO * (1 + Math.max(0,
				computeMaxColumnHeaderTextLength(computations) - 1) / (double) 2));
	}

	private int computeMaxColumnHeaderTextLength(
		List<TaskComputation> computations)
	{
		return computations != null && !computations.isEmpty() ? columnHeader(
			computations.get(computations.size() - 1)).length() : 1;
	}

	private String columnHeader(TaskComputation computation) {
		return computation.getTimepoint() + "";
	}

	private Optional<List<TaskComputation>> getComputations(List<Task> taskList) {
		return taskList.stream().map(Task::getComputations).collect(Collectors
			.<List<TaskComputation>> maxBy((a, b) -> a.size() - b.size()));

	}

	@SuppressWarnings("unchecked")
	private void constructCellFactory(int index) {
		JavaFXRoutines.setCellValueFactoryForList(this.tasks, index, f -> {
			if (f.getValue().getComputations().size() >= index) {
				return new SimpleObservableValue<>(f.getValue().getComputations().get(
					index - 1));
			}
			return null;
		});
		((TableColumn<Task, TaskComputation>) this.tasks.getColumns().get(index))
			.setCellFactory(column -> new TableCellAdapter<>((cell, val, empty) -> {
				if (val == null || empty) {
					cell.setText(EMPTY_VALUE);
					cell.setStyle("");
				}
				else {
					cell.setText(EMPTY_VALUE);
					cell.getStyleClass().add("bordered-class");
					cell.setStyle("-fx-background-color: " + getColorTaskExecState(val
						.getState()));
				}
			}));
	}

}
