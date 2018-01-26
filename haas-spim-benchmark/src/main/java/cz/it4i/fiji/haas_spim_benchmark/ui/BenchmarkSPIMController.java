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
import cz.it4i.fiji.haas.ui.ObservableValueAdapter;
import cz.it4i.fiji.haas.ui.ProgressDialog;
import cz.it4i.fiji.haas.ui.TableViewContextMenu;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.Job;
import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.imagej.updater.util.Progress;

public class BenchmarkSPIMController implements FXFrame.Controller {

	private static boolean notNullValue(Job j, Predicate<Job> pred) {
		if (j == null) {
			return false;
		} else {
			return pred.test(j);
		}
	}

	@FXML
	private TableView<Job> jobs;

	private BenchmarkJobManager manager;

	private Window root;

	private ExecutorService executorService;

	private Timer timer;

	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.BenchmarkSPIMController.class);

	@Override
	public void init(Window frame) {
		executorService = Executors.newSingleThreadExecutor();
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
		TableViewContextMenu<Job> menu = new TableViewContextMenu<>(jobs);
		menu.addItem("Create job", x -> executeJobActionAsync("Creating job", false, p -> manager.createJob()), j -> true);
		menu.addItem("Start job", job -> executeJobActionAsync("Starting job", p -> job.startJob(p)),
				job -> notNullValue(job, j -> j.getState() == JobState.Configuring || j.getState() == JobState.Finished));
		menu.addItem("Download result", job -> executeJobActionAsync("Downloading data", p -> job.downloadData(p)),
				job -> notNullValue(job,
						j -> EnumSet.of(JobState.Failed, JobState.Finished).contains(j.getState()) && !j.downloaded()));
		menu.addItem("Download statistics",
				job -> executeJobActionAsync("Downloading data", p -> job.downloadStatistics(p)),
				job -> notNullValue(job, j -> j.getState() == JobState.Finished));		
		
		menu.addItem("Show output", j -> new JobOutputView(root, executorService, j, Constants.HAAS_UPDATE_TIMEOUT),
				job -> notNullValue(job,
						j -> EnumSet.of(JobState.Failed, JobState.Finished, JobState.Running).contains(j.getState())));
		menu.addItem("Open", j->open(j), x->true);
		menu.addItem("Update table", job -> updateJobs(), j -> true);

	}

	private void open(Job j) {
		executorService.execute(() -> {
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.open(j.getDirectory().toFile());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}});
	}
	
	private void executeJobActionAsync(String title, P_JobAction action) {
		executeJobActionAsync(title, true, action);
	}

	private void executeJobActionAsync(String title, boolean update, P_JobAction action) {
		executorService.execute(() -> {
			try {
				ProgressDialog dialog = ModalDialogs.doModal(new ProgressDialog(root, title),
						WindowConstants.DO_NOTHING_ON_CLOSE);
				action.doAction(dialog);
				dialog.done();
				if(update) {
					updateJobs();
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		});
	}

	private void updateJobs() {
		updateJobs(true);
	}

	private void updateJobs(boolean showProgress) {
		executorService.execute(() -> {
			Progress progress = showProgress
					? ModalDialogs.doModal(new ProgressDialog(root, "Updating jobs"),
							WindowConstants.DO_NOTHING_ON_CLOSE)
					: new DummyProgress();

			Set<Job> old = new HashSet<Job>(jobs.getItems());
			Map<Job, Job> actual;
			try {
				actual = manager.getJobs().stream().map(job -> job.update())
						.collect(Collectors.toMap(job -> job, job -> job));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			for (Job job : old) {
				if (!actual.containsKey(job)) {
					jobs.getItems().remove(job);
				} else {
					job.update(actual.get(job));
				}
			}
			progress.done();
			FXFrame.runOnFxThread(() -> {
				for (Job job : actual.keySet()) {
					if (!old.contains(job)) {
						jobs.getItems().add(job);
					}
				}
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

	@SuppressWarnings("unchecked")
	private void setCellValueFactory(int index, Function<Job, String> mapper) {
		((TableColumn<Job, String>) jobs.getColumns().get(index))
				.setCellValueFactory(f -> new ObservableValueAdapter<Job, String>(f.getValue(), mapper));

	}

	private interface P_JobAction {
		public void doAction(Progress p) throws IOException;
	}

	private void dispose() {
		executorService.shutdown();
		timer.cancel();
	}
}
