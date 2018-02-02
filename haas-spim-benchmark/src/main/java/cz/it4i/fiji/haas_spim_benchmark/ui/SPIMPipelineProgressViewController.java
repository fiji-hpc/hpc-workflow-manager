package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;
import java.util.stream.Collectors;

import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import cz.it4i.fiji.haas_spim_benchmark.core.Task;
import cz.it4i.fiji.haas_spim_benchmark.core.TaskComputation;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;

public class SPIMPipelineProgressViewController implements FXFrame.Controller {

	protected static final String RUNNING_STATE_COMPUTATION = Color.YELLOW.toString();

	protected static final String FINISHED_STATE_COMPUTATION = null;

	protected static final String UNKNOWN_STATE_COMPUTATION = Color.GRAY.toString();

	private static final Map<JobState, Color> taskExecutionState2Color = new HashMap<>();
	static {
		taskExecutionState2Color.put(JobState.Running, Color.YELLOW);
		taskExecutionState2Color.put(JobState.Finished, Color.GREEN);
		taskExecutionState2Color.put(JobState.Failed, Color.RED);
		taskExecutionState2Color.put(JobState.Unknown, Color.GRAY);
	}
	
	private static String getColorTaskExecState(JobState jobState) {
		Color result = null;
		if(jobState == null) {
			result = Color.GRAY;
		} else {
			result = taskExecutionState2Color.get(jobState);
		}
		return toCss(result != null? result : Color.ORANGE);
	}
	
	private static String toCss(Color color) {
		return "rgb(" + Math.round(color.getRed() * 255.0) + "," 
				      + Math.round(color.getGreen() * 255.0) + "," 
				      + Math.round(color.getBlue() * 255.0) + ")";
	}

	@FXML
	private TableView<ObservableValue<Task>> tasks;

	private BenchmarkJob job;
	private Timer timer;
	private ObservableTaskRegistry registry;

	@Override
	public void init(Window frame) {
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				dispose();
			}

		});
		timer = new Timer();
		registry = new ObservableTaskRegistry(task -> tasks.getItems().remove(registry.get(task)));
		fillTable();
	}

	public void setBenchmarkJob(BenchmarkJob job) {
		this.job = job;
	}

	private void fillTable() {
		List<Task> tasks = job.getTasks();
		if (tasks == null) {
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					fillTable();
				}
			}, Constants.HAAS_UPDATE_TIMEOUT / Constants.UI_TO_HAAS_FREQUENCY_UPDATE_RATIO);
		} else {
			
			Optional<List<TaskComputation>> optional = tasks.stream().map(task -> task.getComputations())
					.collect(Collectors.<List<TaskComputation>>maxBy((a, b) -> a.size() - b.size()));
			if(!optional.isPresent()) {
				return;
			}
			List<TaskComputation> computations = optional.get(); 
			int i = 0;
			FXFrame.Controller.setCellValueFactory(this.tasks, i++, (Function<Task, String>) v -> v.getDescription());

			for (TaskComputation tc : computations) {
				this.tasks.getColumns().add(new TableColumn<>(tc.getTimepoint() + ""));
				int index = i++;

				constructCellFactory(index);

			}

			this.tasks.getItems()
					.addAll((tasks.stream().map(task -> registry.addIfAbsent(task)).collect(Collectors.toList())));
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
		FXFrame.Controller.setCellValueFactory(this.tasks, index, (Function<Task, Optional<JobState>>) v -> {
			if (v.getComputations().size() >= index) {
				return Optional.of(v.getComputations().get(index - 1).getState());
			} else {
				return null;
			}
		});
		((TableColumn<ObservableValue<Task>, Optional<JobState>>)this.tasks.getColumns().get(index)).setCellFactory(column->new TableCell<ObservableValue<Task>,Optional<JobState>>(){
			@Override
		    protected void updateItem(Optional<JobState> state, boolean empty) {
				if (state == null || empty) {
		            setText(null);
		            setStyle("");
		        } else {
		            // Format date.
		            setText( "" + (state.isPresent()?state.get():"N/A"));
		            setStyle("-fx-background-color: " + getColorTaskExecState(state.get()));
		        } 
			}
		});
	}

	private void updateTable() {
		registry.update();
	}

	private void dispose() {
		timer.cancel();
	}
}
