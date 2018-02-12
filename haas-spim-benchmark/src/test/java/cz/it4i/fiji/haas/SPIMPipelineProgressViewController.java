package cz.it4i.fiji.haas;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas.ui.ResizeableControl;
import cz.it4i.fiji.haas_java_client.JobState;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class SPIMPipelineProgressViewController extends BorderPane implements CloseableControl, ResizeableControl {

	protected static final String RUNNING_STATE_COMPUTATION = Color.YELLOW.toString();

	protected static final String FINISHED_STATE_COMPUTATION = null;

	protected static final String UNKNOWN_STATE_COMPUTATION = Color.GRAY.toString();

	private final Map<JobState, Color> taskExecutionState2Color = new HashMap<>();
	{
		taskExecutionState2Color.put(JobState.Running, Color.YELLOW);
		taskExecutionState2Color.put(JobState.Finished, Color.GREEN);
		taskExecutionState2Color.put(JobState.Failed, Color.RED);
		taskExecutionState2Color.put(JobState.Unknown, Color.GRAY);
	}

	private String getColorTaskExecState(JobState jobState) {
		Color result = null;
		if (jobState == null) {
			result = Color.GRAY;
		} else {
			result = taskExecutionState2Color.get(jobState);
		}
		return toCss(result != null ? result : Color.ORANGE);
	}

	private static String toCss(Color color) {
		return "rgb(" + Math.round(color.getRed() * 255.0) + "," + Math.round(color.getGreen() * 255.0) + ","
				+ Math.round(color.getBlue() * 255.0) + ")";
	}

	@FXML
	public TableView<ObservableValue<String>> tasks;

	public SPIMPipelineProgressViewController() {
		init();
	}

	public void close() {

	}
	
	@Override
	public void setSize(double width, double height) {
		tasks.setPrefSize(width, height);
	}

	private void init() {
		JavaFXRoutines.initRootAndController("SPIMPipelineProgressView.fxml", this);
		fillTable();

	}

	private void fillTable() {

		JavaFXRoutines.setCellValueFactory(this.tasks, 0, (Function<String, String>) v -> v);
		for (int i = 1; i <= 91; i++) {
			this.tasks.getColumns().add(new TableColumn<>(i + ""));
			constructCellFactory(i);
		}
		for (int i = 0; i < 10; i++) {
			tasks.getItems().add(new ObservableValueBase<String>() {

				@Override
				public String getValue() {
					return "Value";
				}

			});
		}
		
	}

	@SuppressWarnings("unchecked")
	private void constructCellFactory(int index) {
		JavaFXRoutines.setCellValueFactory(this.tasks, index, (Function<String, String>) v -> {
			return v;
		});
		((TableColumn<ObservableValue<String>, String>) this.tasks.getColumns().get(index)).setCellFactory(column -> {
			TableCell<ObservableValue<String>, String> result = new TableCell<ObservableValue<String>, String>() {

				@Override
				protected void updateItem(String computation, boolean empty) {
					if (computation == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText("\u2007\u2007\u2007");
						setStyle("-fx-background-color: " + getColorTaskExecState(JobState.Finished));
					}
				}
			};
			return result;
		});
	}
}
