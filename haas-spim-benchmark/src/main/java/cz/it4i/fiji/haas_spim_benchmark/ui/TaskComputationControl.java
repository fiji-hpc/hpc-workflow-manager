package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas.ui.ModalDialogs;
import cz.it4i.fiji.haas.ui.ProgressDialog;
import cz.it4i.fiji.haas_spim_benchmark.core.FXFrameExecutorService;
import cz.it4i.fiji.haas_spim_benchmark.core.TaskComputation;
import cz.it4i.fiji.haas_spim_benchmark.ui.TaskComputationAdapter.ObservableLog;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
//TASK: context menu udělat pro TaskComputation (buňku) nikoliv řádek - dodělat
//TASK: vyřešit problém při konkurentním scp
public class TaskComputationControl extends TabPane implements CloseableControl, InitiableControl {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.TaskComputationControl.class);

	private TaskComputationAdapter adapter;
	
	private Executor uiExecutor = new FXFrameExecutorService();
	
	private ExecutorService wsExecutorService = Executors.newSingleThreadExecutor();
	@FXML
	private RemoteFilesInfoControl remoteFilesInfo;

	private TaskComputation computation;
	
	public TaskComputationControl(TaskComputation computation) {
		JavaFXRoutines.initRootAndController("TaskComputationView.fxml", this);
		this.computation = computation;
	}
	
	@Override
	public void init(Window parameter) {
		wsExecutorService.execute(() -> {
			ProgressDialog dialog = ModalDialogs.doModal(new ProgressDialog(parameter, "Updating infos..."),
					WindowConstants.DO_NOTHING_ON_CLOSE);
			try {
				adapter = new TaskComputationAdapter(computation);
			} finally {
				dialog.done();
			}
			remoteFilesInfo.setFiles(adapter.getOutputs());
			remoteFilesInfo.init(parameter);
			Collection<Runnable> runs = new LinkedList<>();
			for (ObservableLog log : adapter.getLogs()) {
				LogViewControl logViewControl = new LogViewControl();
				logViewControl.setObservable(log.getContent());
				runs.add(() -> addTab(log.getName(), logViewControl));
			}
			uiExecutor.execute(() -> runs.forEach(r -> r.run()));
		});
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
		if(adapter != null) {
			adapter.close();
		}
		wsExecutorService.shutdown();
	}

	
}
