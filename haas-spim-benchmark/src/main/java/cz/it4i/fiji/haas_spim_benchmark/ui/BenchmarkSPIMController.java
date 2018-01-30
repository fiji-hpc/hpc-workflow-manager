package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.DummyProgress;
import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.haas.ui.ModalDialogs;
import cz.it4i.fiji.haas.ui.ProgressDialog;
import cz.it4i.fiji.haas.ui.TableViewContextMenu;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import cz.it4i.fiji.haas_spim_benchmark.core.FXFrameExecutorService;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import net.imagej.updater.util.Progress;

public class BenchmarkSPIMController implements FXFrame.Controller {

	private static boolean notNullValue(BenchmarkJob j, Predicate<BenchmarkJob> pred) {
		if (j == null) {
			return false;
		} else {
			return pred.test(j);
		}
	}

	@FXML
	private TableView<BenchmarkJob> jobs;

	private BenchmarkJobManager manager;

	private Window root;

	private ExecutorService executorServiceUI;
	private ExecutorService executorServiceWS;
	private Executor executorServiceFX = new FXFrameExecutorService();

	private Timer timer;

	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.BenchmarkSPIMController.class);

	@Override
	public void init(Window frame) {
		executorServiceWS = Executors.newSingleThreadExecutor();
		executorServiceUI = Executors.newSingleThreadExecutor();
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				updateJobs(false);
			}
		}, Constants.HAAS_UPDATE_TIMEOUT, Constants.HAAS_UPDATE_TIMEOUT);
		root = frame;
		initTable();
		initMenu();
		updateJobs();
		root.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				dispose();
			}
		});
	}

	public void setManager(BenchmarkJobManager manager) {
		this.manager = manager;

	}

	private void initMenu() {
		TableViewContextMenu<BenchmarkJob> menu = new TableViewContextMenu<>(jobs);
		menu.addItem("Create job", x -> executeWSCallAsync("Creating job", p -> manager.createJob()), j -> true);
		menu.addItem("Start job", job -> executeWSCallAsync("Starting job", p -> job.startJob(p)),
				job -> notNullValue(job,
						j -> j.getState() == JobState.Configuring || j.getState() == JobState.Finished));

		menu.addItem("Show progress", job -> {
			try {
				new SPIMPipelineProgressViewWindow(root, job).setVisible(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(), e);
			}
		}, job -> notNullValue(job, j -> j.getState() == JobState.Running || j.getState() == JobState.Finished
				|| j.getState() == JobState.Failed));

		menu.addItem("Download result", job -> executeWSCallAsync("Downloading data", p -> job.downloadData(p)),
				job -> notNullValue(job,
						j -> EnumSet.of(JobState.Failed, JobState.Finished).contains(j.getState()) && !j.downloaded()));
		menu.addItem("Download statistics",
				job -> executeWSCallAsync("Downloading data", p -> job.downloadStatistics(p)),
				job -> notNullValue(job, j -> j.getState() == JobState.Finished));

		menu.addItem("Show output", j -> new JobOutputView(root, executorServiceUI, j, Constants.HAAS_UPDATE_TIMEOUT),
				job -> notNullValue(job,
						j -> EnumSet.of(JobState.Failed, JobState.Finished, JobState.Running).contains(j.getState())));
		menu.addItem("Open", j -> open(j), x -> true);
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
		FXFrame.Controller.executeAsync(executorServiceWS, (Callable<Void>) () -> {
			ProgressDialog dialog = ModalDialogs.doModal(new ProgressDialog(root, title),
					WindowConstants.DO_NOTHING_ON_CLOSE);
			action.doAction(dialog);
			dialog.done();
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
		executorServiceUI.execute(() -> {
			Progress progress = showProgress
					? ModalDialogs.doModal(new ProgressDialog(root, "Updating jobs"),
							WindowConstants.DO_NOTHING_ON_CLOSE)
					: new DummyProgress();
			try {
				manager.getJobs().forEach(job -> job.update());
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
			executorServiceUI.execute(() -> {

				Set<BenchmarkJob> old = new HashSet<BenchmarkJob>(jobs.getItems());
				Map<BenchmarkJob, BenchmarkJob> actual;
				try {
					actual = manager.getJobs().stream().collect(Collectors.toMap(job -> job, job -> job));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				for (BenchmarkJob job : old) {
					if (!actual.containsKey(job)) {
						jobs.getItems().remove(job);
					} else {
						job.update(actual.get(job));
					}
				}
				progress.done();
				executorServiceFX.execute(() -> {
					for (BenchmarkJob job : actual.keySet()) {
						if (!old.contains(job)) {
							jobs.getItems().add(job);
						}
					}
				});
			});
		});

	}

	private void initTable() {
		setCellValueFactory(0, j -> j.getId() + "");
		setCellValueFactory(1, j -> j.getState().toString());
		setCellValueFactory(2, j -> j.getCreationTime().toString());
		setCellValueFactory(3, j -> j.getStartTime().toString());
		setCellValueFactory(4, j -> j.getEndTime().toString());
	}

	private void setCellValueFactory(int index, Function<BenchmarkJob, String> mapper) {
		FXFrame.Controller.setCellValueFactory(jobs, index, mapper);
	}

	private interface P_JobAction {
		public void doAction(Progress p) throws IOException;
	}

	private void dispose() {
		executorServiceUI.shutdown();
		executorServiceWS.shutdown();
		timer.cancel();
	}
}
