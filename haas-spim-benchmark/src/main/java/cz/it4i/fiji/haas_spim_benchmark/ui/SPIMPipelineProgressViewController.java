package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
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

public class SPIMPipelineProgressViewController implements FXFrame.Controller {

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

	@SuppressWarnings("unchecked")
	private void fillTable() {
		List<Task> tasks = job.getTasks();
		if (tasks == null) {
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					fillTable();
				}
			}, Constants.HAAS_UPDATE_TIMEOUT / 10);
		} else {
			List<TaskComputation> computations = tasks.stream().map(task -> task.getComputations())
					.collect(Collectors.<List<TaskComputation>>maxBy((a, b) -> a.size() - b.size())).get();
			int i = 0;
			FXFrame.Controller.setCellValueFactory(this.tasks, i++, (Function<Task, String>) v -> v.getDescription());

			for (TaskComputation tc : computations) {
				this.tasks.getColumns().add(new TableColumn<>(tc.getTimepoint() + ""));
				int index = i++;

				FXFrame.Controller.setCellValueFactory(this.tasks, index, (Function<Task, JobState>) v -> {
					if (v.getComputations().size() >= index) {
						return v.getComputations().get(index - 1).getState();
					} else {
						return null;
					}
				});
				((TableColumn<ObservableValue<Task>, JobState>)this.tasks.getColumns().get(index)).setCellFactory(column->new TableCell<ObservableValue<Task>,JobState>(){
					@Override
			        protected void updateItem(JobState state, boolean empty) {
						if (state == null || empty) {
			                setText(null);
			                setStyle("");
			            } else {
			                // Format date.
			                setText(state + "");
			                if(state == JobState.Unknown) {
			                	setStyle("-fx-background-color: yellow");
			                }
			            }
						
					}
				});

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

	private void updateTable() {
		registry.update();
	}

	private void dispose() {
		timer.cancel();
	}
}
