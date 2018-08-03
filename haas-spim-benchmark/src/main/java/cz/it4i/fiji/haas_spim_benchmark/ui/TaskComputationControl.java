
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

import cz.it4i.fiji.commons.UncaughtExceptionHandlerDecorator;
import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas.ui.ModalDialogs;
import cz.it4i.fiji.haas.ui.ProgressDialog;
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

	public final static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_spim_benchmark.ui.TaskComputationControl.class);

	private TaskComputationAdapter adapter;

	private final Executor uiExecutor = new FXFrameExecutorService();

	private ExecutorService wsExecutorService;
	@FXML
	private RemoteFilesInfoControl remoteFilesInfo;

	private final TaskComputation computation;

	private Window rootWindow;

	public TaskComputationControl(final TaskComputation computation) {
		JavaFXRoutines.initRootAndController("TaskComputationView.fxml", this);
		this.computation = computation;
	}

	@Override
	public void init(final Window parameter) {
		this.rootWindow = parameter;
		wsExecutorService = Executors.newSingleThreadExecutor(
			UncaughtExceptionHandlerDecorator.createThreadFactory(
				new AuthFailExceptionHandler(new WindowCloseableAdapter(rootWindow))));

		wsExecutorService.execute(() -> {
			final ProgressDialog dialog = ModalDialogs.doModal(new ProgressDialog(
				parameter, "Updating infos..."), WindowConstants.DO_NOTHING_ON_CLOSE);
			try {
				adapter = new TaskComputationAdapter(computation);
				adapter.init();
			}
			finally {
				dialog.done();
			}
			remoteFilesInfo.setFiles(adapter.getOutputs());
			remoteFilesInfo.init(parameter);
			final Collection<Runnable> runs = new LinkedList<>();
			for (final ObservableLog observableLog : adapter.getLogs()) {
				final LogViewControl logViewControl = new LogViewControl();
				logViewControl.setObservable(observableLog.getContent());
				runs.add(() -> addTab(observableLog.getName(), logViewControl));
			}
			uiExecutor.execute(() -> runs.forEach(r -> r.run()));
		});
	}

	private void addTab(final String title, final Node control) {
		final Tab t = new Tab(title);
		t.setClosable(false);
		final HBox hbox = new HBox();
		HBox.setHgrow(control, Priority.ALWAYS);
		hbox.getChildren().add(control);
		t.setContent(hbox);
		getTabs().add(t);
	}

	@Override
	public void close() {
		adapter.close();
		wsExecutorService.shutdown();
	}

}
