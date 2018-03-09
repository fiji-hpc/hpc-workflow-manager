package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Desktop;
import java.awt.Window;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.DummyProgress;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas.ui.ModalDialogs;
import cz.it4i.fiji.haas.ui.ProgressDialog;
import cz.it4i.fiji.haas.ui.TableViewContextMenu;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import cz.it4i.fiji.haas_spim_benchmark.core.FXFrameExecutorService;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import net.imagej.updater.util.Progress;

//FIXME: fix Exception during context menu request on task with N/A state
public class BenchmarkSPIMController extends BorderPane implements CloseableControl, InitiableControl {

	@FXML
	private TableView<ObservableValue<BenchmarkJob>> jobs;

	private BenchmarkJobManager manager;

	private Window root;

	private ExecutorService executorServiceUI;
	private ExecutorService executorServiceWS;
	private ExecutorService executorServiceJobState = Executors.newWorkStealingPool();
	private Executor executorServiceFX = new FXFrameExecutorService();

	private Timer timer;
	private ObservableBenchmarkJobRegistry registry;

	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.BenchmarkSPIMController.class);

	public BenchmarkSPIMController(BenchmarkJobManager manager) {
		this.manager = manager;
		JavaFXRoutines.initRootAndController("BenchmarkSPIM.fxml", this);

	}

	public void init(Window root) {
		this.root = root;
		executorServiceWS = Executors.newSingleThreadExecutor();
		executorServiceUI = Executors.newSingleThreadExecutor();
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				updateJobs(false);
			}
		}, Constants.HAAS_UPDATE_TIMEOUT, Constants.HAAS_UPDATE_TIMEOUT);
		initTable();
		initMenu();
		executorServiceFX.execute(this::updateJobs);
	}

	private void initMenu() {
		TableViewContextMenu<ObservableValue<BenchmarkJob>> menu = new TableViewContextMenu<>(jobs);
		menu.addItem("Create job", x -> executeWSCallAsync("Creating job", p -> manager.createJob()), j -> true);
		menu.addItem("Start job", job -> executeWSCallAsync("Starting job", p -> {
			job.getValue().startJob(p);
			registry.get(job.getValue()).update();
		}), job -> JavaFXRoutines.notNullValue(job, j -> j.getState() == JobState.Configuring
				|| j.getState() == JobState.Finished || j.getState() == JobState.Failed));

		menu.addItem("Cancel job", job -> executeWSCallAsync("Canceling job", p -> {
			job.getValue().cancelJob();
			registry.get(job.getValue()).update();
		}), job -> JavaFXRoutines.notNullValue(job, j -> j.getState() == JobState.Running));

		menu.addItem("Execution details", job -> {
			try {
				new JobDetailWindow(root, job.getValue()).setVisible(true);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}, job -> JavaFXRoutines.notNullValue(job,
				j -> j.getState() == JobState.Running || j.getState() == JobState.Finished
						|| j.getState() == JobState.Failed || j.getState() == JobState.Canceled));

		menu.addItem("Download result",
				job -> executeWSCallAsync("Downloading data", p -> job.getValue().downloadData(p)),
				job -> JavaFXRoutines.notNullValue(job,
						j -> EnumSet.of(JobState.Failed, JobState.Finished, JobState.Canceled).contains(j.getState())
								&& !j.downloaded()));
		menu.addItem("Download statistics",
				job -> executeWSCallAsync("Downloading data", p -> job.getValue().downloadStatistics(p)),
				job -> JavaFXRoutines.notNullValue(job, j -> j.getState() == JobState.Finished));

		menu.addItem("Explore errors", job -> job.getValue().exploreErrors(),
				job -> JavaFXRoutines.notNullValue(job, j -> j.getState().equals(JobState.Failed)));

		menu.addItem("Open working directory", j -> open(j.getValue()), x -> JavaFXRoutines.notNullValue(x, j -> true));
	}

	private void open(BenchmarkJob j) {
		executorServiceUI.execute(() -> {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.open(j.getDirectory().toFile());
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		});
	}

	private void executeWSCallAsync(String title, P_JobAction action) {
		executeWSCallAsync(title, true, action);
	}

	private void executeWSCallAsync(String title, boolean update, P_JobAction action) {
		JavaFXRoutines.executeAsync(executorServiceWS, (Callable<Void>) () -> {
			ProgressDialog dialog = ModalDialogs.doModal(new ProgressDialog(root, title),
					WindowConstants.DO_NOTHING_ON_CLOSE);
			try {
				action.doAction(dialog);
			} finally {
				dialog.done();
			}
			return null;
		}, x -> {
			if (update)
				updateJobs();
		});
	}

	private void updateJobs() {
		updateJobs(true);
	}

	private void updateJobs(boolean showProgress) {
		if (manager == null) {
			return;
		}
		Progress progress = showProgress
				? ModalDialogs.doModal(new ProgressDialog(root, "Updating jobs"), WindowConstants.DO_NOTHING_ON_CLOSE)
				: new DummyProgress();

		executorServiceWS.execute(() -> {

			try {
				List<BenchmarkJob> jobs = new LinkedList<>(manager.getJobs());
				jobs.sort((bj1, bj2) -> (int) (bj1.getId() - bj2.getId()));
				Set<ObservableValue<BenchmarkJob>> actual = new HashSet<>(this.jobs.getItems());
				for (BenchmarkJob bj : jobs) {
					registry.addIfAbsent(bj);
				}
				registry.update();
				executorServiceFX.execute(() -> {
					for (ObservableValue<BenchmarkJob> value : registry.getAllItems()) {
						if (!actual.contains(value)) {
							this.jobs.getItems().add(value);
						}
					}
				});
				progress.done();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}

		});
	}

	private void initTable() {
		registry = new ObservableBenchmarkJobRegistry(bj -> remove(bj), executorServiceJobState);
		setCellValueFactory(0, j -> j.getId() + "");
		setCellValueFactoryCompletable(1, j -> j.getStateAsync(executorServiceJobState).thenApply(state -> "" + state));
		setCellValueFactory(2, j -> j.getCreationTime().toString());
		setCellValueFactory(3, j -> j.getStartTime().toString());
		setCellValueFactory(4, j -> j.getEndTime().toString());
		// jobs.getSortOrder().add(jobs.getColumns().get(0));
	}

	private void remove(BenchmarkJob bj) {

		jobs.getItems().remove(registry.get(bj));
		bj.remove();
	}

	private void setCellValueFactory(int index, Function<BenchmarkJob, String> mapper) {
		JavaFXRoutines.setCellValueFactory(jobs, index, mapper);
	}

	@SuppressWarnings("unchecked")
	private void setCellValueFactoryCompletable(int index, Function<BenchmarkJob, CompletableFuture<String>> mapper) {
		JavaFXRoutines.setCellValueFactory(jobs, index, mapper);
		((TableColumn<ObservableValue<BenchmarkJob>, CompletableFuture<String>>) jobs.getColumns().get(index))
				.setCellFactory(
						column -> new JavaFXRoutines.TableCellAdapter<ObservableValue<BenchmarkJob>, CompletableFuture<String>>(
								new JavaFXRoutines.FutureValueUpdater<ObservableValue<BenchmarkJob>, String, CompletableFuture<String>>(
										new JavaFXRoutines.StringValueUpdater<ObservableValue<BenchmarkJob>>(),
										executorServiceFX)));
	}

	private interface P_JobAction {
		public void doAction(Progress p) throws IOException;
	}

	public void close() {
		executorServiceUI.shutdown();
		executorServiceWS.shutdown();
		executorServiceJobState.shutdown();
		timer.cancel();
	}
}
