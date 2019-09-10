
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.commons.UncaughtExceptionHandlerDecorator;
import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas_spim_benchmark.core.AuthFailExceptionHandler;
import cz.it4i.fiji.haas_spim_benchmark.core.FXFrameExecutorService;
import cz.it4i.fiji.haas_spim_benchmark.core.TaskComputation;
import cz.it4i.fiji.haas_spim_benchmark.core.WindowCloseableAdapter;
import cz.it4i.fiji.haas_spim_benchmark.ui.TaskComputationAdapter.ObservableLog;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class TaskComputationControl extends TabPane implements CloseableControl,
	InitiableControl
{

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_spim_benchmark.ui.TaskComputationControl.class);

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

	@Override
	public void init(final Window rootWindow) {

		wsExecutorService = Executors.newSingleThreadExecutor(
			UncaughtExceptionHandlerDecorator.createThreadFactory(
				new AuthFailExceptionHandler(new WindowCloseableAdapter(rootWindow))));

		wsExecutorService.execute(() -> {
			ProgressDialogViewWindow progressDialogViewWindow =
				new ProgressDialogViewWindow();
			JavaFXRoutines.runOnFxThread(() -> progressDialogViewWindow.openWindow(
				"Updating information...", true));
			try {
				adapter = new TaskComputationAdapter(computation);
				adapter.init();
			}
			finally {
				JavaFXRoutines.runOnFxThread(progressDialogViewWindow::closeWindow);
			}
			remoteFilesInfo.setFiles(adapter.getOutputs());
			remoteFilesInfo.init(rootWindow);
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

	@Override
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
