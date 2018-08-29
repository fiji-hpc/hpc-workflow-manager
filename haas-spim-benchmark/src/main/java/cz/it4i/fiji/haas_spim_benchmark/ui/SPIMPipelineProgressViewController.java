
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.swing.WindowConstants;

import net.imagej.updater.util.Progress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas.ui.ModalDialogs;
import cz.it4i.fiji.haas.ui.ProgressDialog;
import cz.it4i.fiji.haas.ui.TableCellAdapter;
import cz.it4i.fiji.haas.ui.TableViewContextMenu;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import cz.it4i.fiji.haas_spim_benchmark.core.FXFrameExecutorService;
import cz.it4i.fiji.haas_spim_benchmark.core.ObservableBenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.SimpleObservableList;
import cz.it4i.fiji.haas_spim_benchmark.core.SimpleObservableValue;
import cz.it4i.fiji.haas_spim_benchmark.core.Task;
import cz.it4i.fiji.haas_spim_benchmark.core.TaskComputation;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class SPIMPipelineProgressViewController extends BorderPane implements CloseableControl, InitiableControl {


	public final static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.SPIMPipelineProgressViewController.class);
	
	private static final String EMPTY_VALUE = "";

	private static final int PREFERRED_WIDTH = 900;

	private static final Map<JobState, Color> taskExecutionState2Color = new HashMap<>();

	private static final double TIMEPOINT_TABLE_COLUMN_WIDTH_RATIO = 6;
	static {
		taskExecutionState2Color.put(JobState.Running, Color.rgb(0xF2, 0xD5, 0x39));
		taskExecutionState2Color.put(JobState.Finished, Color.rgb(0x41, 0xB2, 0x80));
		taskExecutionState2Color.put(JobState.Failed, Color.rgb(0xFF, 0x51, 0x3D));
		taskExecutionState2Color.put(JobState.Queued, Color.rgb(0x30, 0xA2, 0xCC));
		taskExecutionState2Color.put(JobState.Unknown, Color.GRAY);
	}

	private static String getColorTaskExecState(JobState jobState) {
		Color result = null;
		if (jobState == null) {
			result = Color.GRAY;
		} else {
			result = taskExecutionState2Color.get(jobState);
		}
		return JavaFXRoutines.toCss(result != null ? result : Color.ORANGE);
	}

	@FXML
	private TableView<Task> tasks;

	private SimpleObservableList<Task> observedValue;

	private final ExecutorService executorServiceWS;
	private final Executor executorFx = new FXFrameExecutorService();
	private Window root;

	private boolean closed;

	private final ListChangeListener<Task> taskChangeListener =
		new ListChangeListener<Task>()
		{

			@Override
			public void onChanged(Change<? extends Task> c) {
				fillTable();
			}

		};

	public SPIMPipelineProgressViewController() {
		executorServiceWS = Executors.newSingleThreadExecutor();
		init();
	}

	@Override
	public synchronized void close() {
		observedValue.removeListenerWithCallback(taskChangeListener);
		executorServiceWS.shutdown();
		closed = true;
	}

	@Override
	public void init(Window parameter) {
		this.root = parameter;
	}

	public void setJob(final ObservableBenchmarkJob job) {

		observedValue = job.getObservableTaskList();
		observedValue.addListenerWithCallback(taskChangeListener);

		Progress progress = ModalDialogs.doModal(new ProgressDialog(root,
			"Downloading tasks"), WindowConstants.DO_NOTHING_ON_CLOSE);

		executorServiceWS.execute(() -> {
			try {
				fillTable();
			}
			finally {
				progress.done();
			}
		});
	}

	private void init() {
		JavaFXRoutines.initRootAndController("SPIMPipelineProgressView.fxml", this);
		tasks.setPrefWidth(PREFERRED_WIDTH);

		TableViewContextMenu<Task> menu = new TableViewContextMenu<>(this.tasks);
		menu.addItem("Open view", (task, columnIndex) -> proof(task, columnIndex), (
			x, columnIndex) -> check(x, columnIndex));
	}

	private boolean check(Task x, Integer columnIndex) {
		boolean result = x != null && 0 < columnIndex && columnIndex - 1 < x
			.getComputations().size();
		return result;
	}

	private void proof(Task task, int columnIndex) {
		ModalDialogs.doModal(new TaskComputationWindow(root, task.getComputations()
			.get(columnIndex - 1)), WindowConstants.DISPOSE_ON_CLOSE);
	}

	private synchronized void fillTable() {
		if (closed) {
			return;
		}

		final Optional<List<TaskComputation>> optional = observedValue.stream().map(
			task -> task.getComputations()).collect(Collectors
				.<List<TaskComputation>> maxBy((a, b) -> a.size() - b.size()));
		if (!optional.isPresent()) {
			return;
		}
		final List<TaskComputation> computations = optional.get();

		executorFx.execute(() -> {
			int i = 0;
			JavaFXRoutines.setCellValueFactoryForList(this.tasks, i++,
				f -> new SimpleObservableValue<>(Constants.BENCHMARK_TASK_NAME_MAP.get(f
					.getValue().getDescription())));

			double tableColumnWidth = computeTableColumnWidth(computations);
			for (TaskComputation tc : computations) {
				TableColumn<Task, String> tableCol;
				this.tasks.getColumns().add(tableCol = new TableColumn<>(columnHeader(
					tc)));
				int index = i++;
				tableCol.setPrefWidth(tableColumnWidth);
				constructCellFactory(index);
			}

			this.tasks.setItems(observedValue);
		});
	}

	private long computeTableColumnWidth(List<TaskComputation> computations) {
		return Math.round(this.tasks.getColumns().get(0).getWidth() /
			TIMEPOINT_TABLE_COLUMN_WIDTH_RATIO * (1 + Math.max(0,
				computeMaxColumnHeaderTextLength(computations) - 1) / 2));
	}

	private int computeMaxColumnHeaderTextLength(
		List<TaskComputation> computations)
	{
		return computations != null && computations.size() > 0 ? columnHeader(
			computations.get(computations.size() - 1)).length() : 1;
	}

	private String columnHeader(TaskComputation taskComputation) {
		return taskComputation.getTimepoint() + "";
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
					} else {
						cell.setText(EMPTY_VALUE);
						cell.getStyleClass().add("bordered-class");
						cell.setStyle("-fx-background-color: " + getColorTaskExecState(val.getState()));
					}
				}));
	}

}
