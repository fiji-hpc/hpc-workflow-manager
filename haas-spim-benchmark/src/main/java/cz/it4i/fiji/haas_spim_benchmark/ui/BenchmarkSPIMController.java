package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Desktop;
import java.awt.Window;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.DummyProgress;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.ModalDialogs;
import cz.it4i.fiji.haas.ui.ProgressDialog;
import cz.it4i.fiji.haas.ui.TableViewContextMenu;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import cz.it4i.fiji.haas_spim_benchmark.core.FXFrameExecutorService;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import net.imagej.updater.util.Progress;

public class BenchmarkSPIMController extends BorderPane implements CloseableControl,InitiableControl{

	private static boolean notNullValue(ObservableValue<BenchmarkJob> j, Predicate<BenchmarkJob> pred) {
		if (j == null || j.getValue() == null) {
			return false;
		} else {
			return pred.test(j.getValue());
		}
	}

	@FXML
	private TableView<ObservableValue<BenchmarkJob>> jobs;

	private BenchmarkJobManager manager;

	private Window root;

	private ExecutorService executorServiceUI;
	private ExecutorService executorServiceWS;
	private Executor executorServiceFX = new FXFrameExecutorService();

	private Timer timer;
	private ObservableBenchmarkJobRegistry registry;

	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.BenchmarkSPIMController.class);

	public BenchmarkSPIMController(BenchmarkJobManager manager) {
		this.manager = manager;
		CloseableControl.initRootAndController("BenchmarkSPIM.fxml", this);
		
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
		updateJobs();
		
	}

	private void initMenu() {
		TableViewContextMenu<ObservableValue<BenchmarkJob>> menu = new TableViewContextMenu<>(jobs);
		menu.addItem("Create job", x -> executeWSCallAsync("Creating job", p -> manager.createJob()), j -> true);
		menu.addItem("Start job", job -> executeWSCallAsync("Starting job", p -> {
			job.getValue().startJob(p);
			registry.get(job.getValue()).update();
		}), job -> notNullValue(job, j -> j.getState() == JobState.Configuring || j.getState() == JobState.Finished
				|| j.getState() == JobState.Failed));

		menu.addItem("Cancel job", job -> executeWSCallAsync("Canceling job", p -> {
			job.getValue().cancelJob();
			registry.get(job.getValue()).update();
		}), job -> notNullValue(job, j -> j.getState() == JobState.Running));

		menu.addItem("Show progress", job -> {
			try {
				new SPIMPipelineProgressViewWindow(root, job.getValue()).setVisible(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(), e);
			}
		}, job -> notNullValue(job, j -> j.getState() == JobState.Running || j.getState() == JobState.Finished
				|| j.getState() == JobState.Failed));

		menu.addItem("Download result",
				job -> executeWSCallAsync("Downloading data", p -> job.getValue().downloadData(p)),
				job -> notNullValue(job,
						j -> EnumSet.of(JobState.Failed, JobState.Finished, JobState.Canceled).contains(j.getState())
								&& !j.downloaded()));
		menu.addItem("Download statistics",
				job -> executeWSCallAsync("Downloading data", p -> job.getValue().downloadStatistics(p)),
				job -> notNullValue(job, j -> j.getState() == JobState.Finished));

		menu.addItem("Explore errors", job -> job.getValue().exploreErrors(),
				job -> notNullValue(job, j -> j.getState().equals(JobState.Failed)));

		menu.addItem("Show output",
				j -> {
					new JobOutputView(root, executorServiceUI, j.getValue(),SynchronizableFileType.StandardErrorFile, Constants.HAAS_UPDATE_TIMEOUT);
					new JobOutputView(root, executorServiceUI, j.getValue(),SynchronizableFileType.StandardOutputFile, Constants.HAAS_UPDATE_TIMEOUT);
				},
				job -> notNullValue(job,
						j -> EnumSet.of(JobState.Failed, JobState.Finished, JobState.Running, JobState.Canceled)
								.contains(j.getState())));
		menu.addItem("Open working directory", j -> open(j.getValue()), x -> notNullValue(x, j -> true));
		menu.addItem("Update table", job -> updateJobs(), j -> true);

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
		CloseableControl.executeAsync(executorServiceWS, (Callable<Void>) () -> {
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
		executorServiceWS.execute(() -> {
			Progress progress = showProgress
					? ModalDialogs.doModal(new ProgressDialog(root, "Updating jobs"),
							WindowConstants.DO_NOTHING_ON_CLOSE)
					: new DummyProgress();

			registry.update();
			try {
				Collection<BenchmarkJob> jobs = manager.getJobs();
				
					Set<ObservableValue<BenchmarkJob>> actual = new HashSet<>(this.jobs.getItems());
					for (BenchmarkJob bj : jobs) {
					ObservableValue<BenchmarkJob> value = registry.addIfAbsent(bj);
					executorServiceFX.execute(() -> {
						if (!actual.contains(value)) {
							this.jobs.getItems().add(value);
						}
					});
				}
				progress.done();
				
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}

		});
	}

	private void initTable() {
		registry = new ObservableBenchmarkJobRegistry(bj -> remove(bj));
		setCellValueFactory(0, j -> j.getId() + "");
		setCellValueFactory(1, j -> j.getState().toString());
		setCellValueFactory(2, j -> j.getCreationTime().toString());
		setCellValueFactory(3, j -> j.getStartTime().toString());
		setCellValueFactory(4, j -> j.getEndTime().toString());
	}

	private void remove(BenchmarkJob bj) {

		jobs.getItems().remove(registry.get(bj));
		bj.remove();
	}

	private void setCellValueFactory(int index, Function<BenchmarkJob, String> mapper) {
		CloseableControl.setCellValueFactory(jobs, index, mapper);
	}

	private interface P_JobAction {
		public void doAction(Progress p) throws IOException;
	}

	public void close() {
		executorServiceUI.shutdown();
		executorServiceWS.shutdown();
		timer.cancel();
	}
}
