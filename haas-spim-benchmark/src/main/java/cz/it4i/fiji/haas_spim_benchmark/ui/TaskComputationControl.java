package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas_spim_benchmark.core.TaskComputation;
import cz.it4i.fiji.haas_spim_benchmark.ui.TaskComputationAdapter.ObservableLog;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
//TASK: context menu udělat pro TaskComputation (buňku) nikoliv řádek
//TASK: TaskComputationWindow - vyřešit vlákna na kterých se to spouští
//TASK: dodělat progress dialog + modalita
//TASK: vyřešit problém při konkurentním scp
//TASK: TaskComputationWindow - iniciální velikost okna
public class TaskComputationControl extends TabPane implements CloseableControl, InitiableControl {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.TaskComputationControl.class);

	private final TaskComputationAdapter adapter;
	
	public TaskComputationControl(TaskComputation computation) {
		JavaFXRoutines.initRootAndController("TaskComputationView.fxml", this);
		adapter = new TaskComputationAdapter(computation);
		
	}
	
	@Override
	public void init(Window parameter) {
		RemoteFilesInfoControl infoControl=  new RemoteFilesInfoControl(adapter.getOutputs());
		infoControl.init(parameter);
		addTab("Output files", infoControl);
		for (ObservableLog log: adapter.getLogs()) {
			LogViewControl logViewControl = new LogViewControl();
			logViewControl.setObservable(log.getContent());
			addTab(log.getName(), logViewControl);
		}
	}

	private void addTab(String title, Node control) {
		Tab t = new Tab(title);
		t.setClosable(false);
		HBox hbox = new HBox();
		HBox.setHgrow(control, Priority.ALWAYS);
		hbox.getChildren().add(control);
		t.setContent(hbox);
		getTabs().add(t);
	}
	@Override
	public void close() {
		adapter.close();
	}

	
}
