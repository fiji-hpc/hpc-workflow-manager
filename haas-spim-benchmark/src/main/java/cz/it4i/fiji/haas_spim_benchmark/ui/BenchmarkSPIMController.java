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
import cz.it4i.fiji.haas_spim_benchmark.core.ObservableBenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.ObservableBenchmarkJob.TransferProgress;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import net.imagej.updater.util.Progress;

//FIXME: fix Exception during context menu request on task with N/A state
public class BenchmarkSPIMController extends BorderPane implements CloseableControl, InitiableControl {

	@FXML
	private TableView<ObservableBenchmarkJob> jobs;

	private final BenchmarkJobManager manager;

	private final ExecutorService executorServiceJobState = Executors.newWorkStealingPool();

	private final Executor executorServiceFX = new FXFrameExecutorService();

	private Window root;

	private ExecutorService executorServiceUI;

	private ExecutorService executorServiceWS;

	private Timer timer;

	private ObservableBenchmarkJobRegistry registry;

	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.BenchmarkSPIMController.class);

	public BenchmarkSPIMController(BenchmarkJobManager manager) {
		this.manager = manager;
		JavaFXRoutines.initRootAndController("BenchmarkSPIM.fxml", this);

	}

	@Override
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

	@Override
	public void close() {
		executorServiceUI.shutdown();
		executorServiceWS.shutdown();
		executorServiceJobState.shutdown();
		timer.cancel();
		manager.close();
	}

	private void initMenu() {
		TableViewContextMenu<ObservableBenchmarkJob> menu = new TableViewContextMenu<>(jobs);
		menu.addItem("Create job", x -> createJob(), j -> true);
		menu.addItem("Start job", job -> executeWSCallAsync("Starting job", p -> {
			job.getValue().startJob(p);
			job.getValue().update();
		}), job -> JavaFXRoutines.notNullValue(job, j -> j.getState() == JobState.Configuring
				|| j.getState() == JobState.Finished || j.getState() == JobState.Failed));

		menu.addItem("Cancel job", job -> executeWSCallAsync("Canceling job", p -> {
			job.getValue().cancelJob();
			job.getValue().update();
		}), job -> JavaFXRoutines.notNullValue(job,
				j -> j.getState() == JobState.Running || j.getState() == JobState.Queued));

		menu.addItem("Execution details", job -> {
			try {
				new JobDetailWindow(root, job.getValue()).setVisible(true);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}, job -> JavaFXRoutines.notNullValue(job,
				j -> j.getState() == JobState.Running || j.getState() == JobState.Finished
						|| j.getState() == JobState.Failed || j.getState() == JobState.Canceled));

		menu.addItem("Upload data", job -> executeWSCallAsync("Uploading data", p -> job.getValue().startUpload()),
				job -> executeWSCallAsync("Stop uploading data", p -> job.getValue().stopUpload()),
				job -> JavaFXRoutines.notNullValue(job,
						j -> !j.isUseDemoData() && !EnumSet.of(JobState.Running).contains(j.getState())),
				job -> job.getUploadProgress().isWorking());

		menu.addItem("Download result",
				job -> executeWSCallAsync("Downloading data", p -> job.getValue().startDownload()),
				job -> executeWSCallAsync("Stop downloading data", p -> job.getValue().stopDownload()),
				job -> JavaFXRoutines
						.notNullValue(job,
								j -> EnumSet.of(JobState.Failed, JobState.Finished, JobState.Canceled)
										.contains(j.getState()) && j.canBeDownloaded()),
				job -> job.getDownloadProgress().isWorking());

		menu.addItem("Download statistics",
				job -> executeWSCallAsync("Downloading data", p -> job.getValue().downloadStatistics(p)),
				job -> JavaFXRoutines.notNullValue(job, j -> j.getState() == JobState.Finished));

		menu.addItem("Explore errors", job -> job.getValue().exploreErrors(),
				job -> JavaFXRoutines.notNullValue(job, j -> j.getState().equals(JobState.Failed)));

		menu.addItem("Open working directory", j -> open(j.getValue()), x -> JavaFXRoutines.notNullValue(x, j -> true));
	}

	private void createJob() {
		NewJobWindow newJobWindow = new NewJobWindow(null);
		ModalDialogs.doModal(newJobWindow, WindowConstants.DISPOSE_ON_CLOSE);
		newJobWindow.setCreatePressedNotifier(() -> executeWSCallAsync("Creating job", p -> manager
				.createJob(wd -> newJobWindow.getInputDirectory(wd), wd -> newJobWindow.getOutputDirectory(wd))));

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
				for (BenchmarkJob bj : jobs) {
					registry.addIfAbsent(bj);
				}
				registry.update();
				Set<ObservableValue<BenchmarkJob>> actual = new HashSet<>(this.jobs.getItems());

				executorServiceFX.execute(() -> {
					for (ObservableBenchmarkJob value : registry.getAllItems()) {
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
		registry = new ObservableBenchmarkJobRegistry(bj -> remove(bj), executorServiceJobState, executorServiceFX);
		setCellValueFactory(0, j -> j.getId() + "");
		setCellValueFactoryCompletable(1, j -> j.getStateAsync(executorServiceJobState).thenApply(state -> "" + state));
		setCellValueFactory(2, j -> j.getCreationTime().toString());
		setCellValueFactory(3, j -> j.getStartTime().toString());
		setCellValueFactory(4, j -> j.getEndTime().toString());
		setCellValueFactory(5, j -> decorateTransfer(registry.get(j).getUploadProgress()));
		setCellValueFactory(6, j -> decorateTransfer(registry.get(j).getDownloadProgress()));
	}

	private String decorateTransfer(TransferProgress progress) {
		if (!progress.isWorking() && !progress.isDone()) {
			return "";
		} else if (progress.isWorking()) {
			Long msecs = progress.getRemainingMiliseconds();
			return "Time remains " + (msecs != null ? RemainingTimeFormater.format(msecs) : "N/A");
		} else if (progress.isDone()) {
			return "Done";
		}
		return "N/A";
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
		((TableColumn<ObservableBenchmarkJob, CompletableFuture<String>>) jobs.getColumns().get(index)).setCellFactory(
				column -> new JavaFXRoutines.TableCellAdapter<ObservableBenchmarkJob, CompletableFuture<String>>(
						new JavaFXRoutines.FutureValueUpdater<ObservableBenchmarkJob, String, CompletableFuture<String>>(
								new JavaFXRoutines.StringValueUpdater<ObservableBenchmarkJob>(), executorServiceFX)));
	}

	private interface P_JobAction {
		public void doAction(Progress p) throws IOException;
	}
}
