package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas.ui.ModalDialogs;
import cz.it4i.fiji.haas.ui.TableCellAdapter;
import cz.it4i.fiji.haas.ui.TableViewContextMenu;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import cz.it4i.fiji.haas_spim_benchmark.core.FXFrameExecutorService;
import cz.it4i.fiji.haas_spim_benchmark.core.ObservableBenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.Task;
import cz.it4i.fiji.haas_spim_benchmark.core.TaskComputation;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class SPIMPipelineProgressViewController extends BorderPane implements CloseableControl, InitiableControl {

	public final static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.SPIMPipelineProgressViewController.class);
	
	private static final String EMPTY_VALUE = "\u2007\u2007\u2007";

	private static final int PREFERRED_WIDTH = 900;

	private static final Map<JobState, Color> taskExecutionState2Color = new HashMap<>();
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
	private TableView<ObservableValue<Task>> tasks;

	private ObservableBenchmarkJob job;
	private Timer timer;
	private ObservableTaskRegistry registry;
	private final ExecutorService executorServiceWS;
	private final Executor executorFx = new FXFrameExecutorService();
	private Window root;

	public SPIMPipelineProgressViewController() {
		executorServiceWS = Executors.newSingleThreadExecutor();
		init();
	}

	public void setJob(final ObservableBenchmarkJob job) {
		if (this.job != null) {
			throw new IllegalStateException("Job already set");
		}
		this.job = job;
		executorServiceWS.execute(() -> {
			fillTable();
		});
	}

	@Override
	public void close() {
		timer.cancel();
		executorServiceWS.shutdown();
	}

	@Override
	public void init(Window parameter) {
		this.root = parameter;
	}

	private void init() {
		JavaFXRoutines.initRootAndController("SPIMPipelineProgressView.fxml", this);
		tasks.setPrefWidth(PREFERRED_WIDTH);
		timer = new Timer();
		registry = new ObservableTaskRegistry(()-> job, task -> tasks.getItems().remove(registry.get(task)));
		TableViewContextMenu<ObservableValue<Task>> menu = new TableViewContextMenu<>(this.tasks);
		menu.addItem("Open view", (task, columnIndex) -> proof(task, columnIndex),
				(x, columnIndex) -> check(x, columnIndex));
	}

	private boolean check(ObservableValue<Task> x, Integer columnIndex) {
		boolean result = x != null && 0 < columnIndex &&columnIndex - 1 < x.getValue().getComputations().size();
		return result;
	}

	private void proof(ObservableValue<Task> task, int columnIndex) {
		ModalDialogs.doModal(new TaskComputationWindow(root, task.getValue().getComputations().get(columnIndex - 1)),
				WindowConstants.DISPOSE_ON_CLOSE);
	}

	static void add(Collection<ObservableValue<RemoteFileInfo>> files, String name, long size) {
		RemoteFileInfo file = new RemoteFileInfo() {

			@Override
			public Long getSize() {
				return size;
			}

			@Override
			public String getName() {
				return name;
			}

		};
		ObservableValue<RemoteFileInfo> value = new ObservableValueBase<RemoteFileInfo>() {

			@Override
			public RemoteFileInfo getValue() {
				return file;
			}
		};

		files.add(value);
	}

	private void fillTable() {
		List<Task> processedTasks = job.getValue().getTasks();
		if (processedTasks == null) {
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					fillTable();
				}
			}, Constants.HAAS_UPDATE_TIMEOUT / Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO);
		} else {

			Optional<List<TaskComputation>> optional = processedTasks.stream().map(task -> task.getComputations())
					.collect(Collectors.<List<TaskComputation>>maxBy((a, b) -> a.size() - b.size()));
			if (!optional.isPresent()) {
				return;
			}
			List<TaskComputation> computations = optional.get();
			List<ObservableValue<Task>> taskList = (processedTasks.stream().map(task -> registry.addIfAbsent(task))
					.collect(Collectors.toList()));
			executorFx.execute(() -> {
				int i = 0;
				JavaFXRoutines.setCellValueFactory(this.tasks, i++,
						(Function<Task, String>) v -> Constants.BENCHMARK_TASK_NAME_MAP.get(v.getDescription()));
				for (TaskComputation tc : computations) {
					this.tasks.getColumns().add(new TableColumn<>(tc.getTimepoint() + ""));
					int index = i++;
					constructCellFactory(index);
				}
				fixNotVisibleColumn();
				this.tasks.getItems().addAll(taskList);
			});
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					updateTable();
				}
			}, Constants.HAAS_UPDATE_TIMEOUT, Constants.HAAS_UPDATE_TIMEOUT);
		}
	}

	@SuppressWarnings("unchecked")
	private void constructCellFactory(int index) {
		JavaFXRoutines.setCellValueFactory(this.tasks, index, (Function<Task, TaskComputation>) v -> {
			if (v.getComputations().size() >= index) {
				return v.getComputations().get(index - 1);
			} 
			return null;
		});
		((TableColumn<ObservableValue<Task>, TaskComputation>) this.tasks.getColumns().get(index))
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

	private void updateTable() {
		registry.update();
	}

	private void fixNotVisibleColumn() {
		// this.tasks.getColumns().add(new TableColumn<>(" "));
	}
}
