
package cz.it4i.fiji.hpc_workflow.ui;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.common.UncaughtExceptionHandlerDecorator;
import cz.it4i.fiji.hpc_workflow.TaskComputation;
import cz.it4i.fiji.hpc_workflow.core.AuthFailExceptionHandler;
import cz.it4i.fiji.hpc_workflow.core.FXFrameExecutorService;
import cz.it4i.fiji.hpc_workflow.ui.TaskComputationAdapter.ObservableLog;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class TaskComputationControl extends TabPane
{

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_workflow.ui.TaskComputationControl.class);

	private TaskComputationAdapter adapter;

	private final Executor uiExecutor = new FXFrameExecutorService();

	private ExecutorService wsExecutorService;

	@FXML
	private RemoteFilesInfoControl remoteFilesInfo;

	private final TaskComputation computation;

	public TaskComputationControl(final TaskComputation computation) {
		JavaFXRoutines.initRootAndController("TaskComputationView.fxml", this);
		this.computation = computation;
	}

	// -- InitiableControl methods --

	public void init(final Stage parentStage) {

		wsExecutorService = Executors.newSingleThreadExecutor(
			UncaughtExceptionHandlerDecorator.createThreadFactory(
				new AuthFailExceptionHandler()));

		wsExecutorService.execute(() -> {
			ProgressDialogViewWindow progress = new ProgressDialogViewWindow(
				"Updating information...", null);
			try {
				adapter = new TaskComputationAdapter(computation);
				adapter.init();
			}
			finally {
				progress.done();
			}
			remoteFilesInfo.setFiles(adapter.getOutputs());
			remoteFilesInfo.init(parentStage);
			final Collection<Runnable> runnables = new LinkedList<>();
			for (final ObservableLog observableLog : adapter.getLogs()) {
				final LogViewControl logViewControl = new LogViewControl();
				logViewControl.setObservable(observableLog.getContent());
				runnables.add(() -> addTab(observableLog.getName(), logViewControl));
			}
			uiExecutor.execute(() -> runnables.forEach(Runnable::run));
		});
	}

	// -- CloseableControl methods --

	public void close() {
		adapter.close();
		wsExecutorService.shutdown();
	}

	// -- Helper methods --

	private void addTab(final String title, final Node control) {
		final Tab t = new Tab(title);
		t.setClosable(false);
		final HBox hbox = new HBox();
		HBox.setHgrow(control, Priority.ALWAYS);
		hbox.getChildren().add(control);
		t.setContent(hbox);
		getTabs().add(t);
	}

}
