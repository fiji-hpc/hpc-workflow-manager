
package cz.it4i.fiji.haas_spim_benchmark.ui;

import static cz.it4i.fiji.haas_spim_benchmark.core.Configuration.getHaasUpdateTimeout;
import static cz.it4i.fiji.haas_spim_benchmark.core.Constants.CONFIG_YAML;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.swing.WindowConstants;

import net.imagej.updater.util.Progress;

import org.scijava.Context;
import org.scijava.ui.swing.script.TextEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.BigDataViewer;
import bdv.export.ProgressWriterConsole;
import bdv.viewer.ViewerOptions;
import cz.it4i.fiji.haas.UploadingFileFromResource;
import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.DummyProgress;
import cz.it4i.fiji.haas.ui.FutureValueUpdater;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas.ui.ModalDialogs;
import cz.it4i.fiji.haas.ui.ProgressDialog;
import cz.it4i.fiji.haas.ui.ShellRoutines;
import cz.it4i.fiji.haas.ui.StringValueUpdater;
import cz.it4i.fiji.haas.ui.TableCellAdapter;
import cz.it4i.fiji.haas.ui.TableCellAdapter.TableCellUpdater;
import cz.it4i.fiji.haas.ui.TableViewContextMenu;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.UploadingFile;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import cz.it4i.fiji.haas_spim_benchmark.core.FXFrameExecutorService;
import cz.it4i.fiji.haas_spim_benchmark.core.ObservableBenchmarkJob;
import cz.it4i.fiji.haas_spim_benchmark.core.ObservableBenchmarkJob.TransferProgress;
import cz.it4i.fiji.haas_spim_benchmark.ui.NewJobController.WorkflowType;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import mpicbg.spim.data.SpimDataException;

public class BenchmarkSPIMControl extends BorderPane implements
	CloseableControl, InitiableControl
{

	@FXML
	private TableView<ObservableBenchmarkJob> jobs;

	private final BenchmarkJobManager manager;

	private final ExecutorService executorServiceJobState = Executors
		.newWorkStealingPool();

	private final Executor executorServiceFX = new FXFrameExecutorService();

	private Window root;

	private ExecutorService executorServiceShell;

	private ExecutorService executorServiceWS;

	private Timer timer;

	private ObservableBenchmarkJobRegistry registry;

	private final JobStateNameProvider provider = new JobStateNameProvider();

	private boolean closed;

	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_spim_benchmark.ui.BenchmarkSPIMControl.class);

	public BenchmarkSPIMControl(BenchmarkJobManager manager) {
		this.manager = manager;
		JavaFXRoutines.initRootAndController("BenchmarkSPIM.fxml", this);
		jobs.setPlaceholder(new Label(
			"No content in table. Right click to create new one."));
	}

	@Override
	public void init(Window rootWindow) {
		this.root = rootWindow;
		executorServiceWS = Executors.newSingleThreadExecutor();
		executorServiceShell = Executors.newSingleThreadExecutor();
		timer = new Timer();
		initTable();
		initMenu();
		boolean result = checkConnection();
		synchronized (this) {
			if (result && !closed) {
				timer.scheduleAtFixedRate(new TimerTask() {

					@Override
					public void run() {
						updateJobs(false);
					}
				}, getHaasUpdateTimeout(), getHaasUpdateTimeout());
				updateJobs(true);
			}
		}
	}

	@Override
	public synchronized void close() {
		if (!closed) {
			executorServiceShell.shutdown();
			executorServiceWS.shutdown();
			executorServiceJobState.shutdown();
			registry.close();
			timer.cancel();
			manager.close();
			closed = true;
		}
	}

	private void initMenu() {
		TableViewContextMenu<ObservableBenchmarkJob> menu =
			new TableViewContextMenu<>(jobs);
		menu.addItem("Create a new job", x -> askForCreateJob(), j -> true);
		menu.addSeparator();

		menu.addItem("Start job", job -> {
			executeWSCallAsync("Starting job", p -> {
				job.getValue().startJob(p);
				job.getValue().update();
			});
		}, job -> JavaFXRoutines.notNullValue(job, j -> (j
			.getState() == JobState.Configuring || j
				.getState() == JobState.Finished || j.getState() == JobState.Failed || j
					.getState() == JobState.Canceled) && checkIfMacroScriptIsUploaded(
						j)));

		menu.addItem("Cancel job", job -> executeWSCallAsync("Canceling job", p -> {
			job.getValue().cancelJob();
			job.getValue().update();
		}), job -> JavaFXRoutines.notNullValue(job, j -> j
			.getState() == JobState.Running || j.getState() == JobState.Queued));

		menu.addItem("Job dashboard", this::openJobDetailsWindow,
			job -> JavaFXRoutines.notNullValue(job, j -> true));
		menu.addItem("Open job subdirectory", j -> openJobSubdirectory(j
			.getValue()), x -> JavaFXRoutines.notNullValue(x, j -> true));
		menu.addItem("Open in BigDataViewer", j -> openBigDataViewer(j.getValue()),
			x -> JavaFXRoutines.notNullValue(x, j -> j
				.getState() == JobState.Finished && j.isVisibleInBDV()));
		menu.addItem("Open macro in Editor", j -> openEditor(j.getValue()),
			x -> JavaFXRoutines.notNullValue(x, j -> j
				.getWorkflowType() == NewJobController.WorkflowType.MACRO_WORKFLOW));

		menu.addSeparator();

		menu.addItem("Upload data", job -> {
			createTheMacroScript(job);
			executeWSCallAsync("Uploading data", p -> job.getValue().startUpload());
		}, job -> executeWSCallAsync("Stop uploading data", p -> job.getValue()
			.stopUpload()), job -> JavaFXRoutines.notNullValue(job, j -> !j
				.isUseDemoData() && !EnumSet.of(JobState.Running, JobState.Disposed)
					.contains(j.getState())), job -> job != null && job
						.getUploadProgress().isWorking());

		menu.addItem("Download result", job -> executeWSCallAsync(
			"Downloading data", p -> job.getValue().startDownload()),
			job -> executeWSCallAsync("Stop downloading data", p -> job.getValue()
				.stopDownload()), job -> JavaFXRoutines.notNullValue(job, j -> EnumSet
					.of(JobState.Failed, JobState.Finished, JobState.Canceled).contains(j
						.getState()) && j.canBeDownloaded()), job -> job != null && job
							.getDownloadProgress().isWorking());

		menu.addItem("Explore errors", job -> job.getValue().exploreErrors(),
			job -> JavaFXRoutines.notNullValue(job, j -> j.getState().equals(
				JobState.Failed)));

		menu.addSeparator();

		menu.addItem("Delete job", j -> deleteJob(j.getValue()), x -> JavaFXRoutines
			.notNullValue(x, j -> j.getState() != JobState.Running));

	}

	private boolean checkIfMacroScriptIsUploaded(BenchmarkJob job) {
		if (job.getWorkflowType() == WorkflowType.MACRO_WORKFLOW) {
			// If the script file does not exist return false:
			List<String> files = new ArrayList<>();
			files.add(Constants.DEFAULT_MACRO_FILE);
			String file = job.getFileContents(files).get(0);
			if (file.isEmpty()) {
				return false;
			}
		}	
		// If it is not a Macro Workflow then this method should have no impact
		// therefore it returns true:
		return true;
	}

	private void createTheMacroScript(ObservableBenchmarkJob job) {
		if (job.getWorkflowType() == WorkflowType.MACRO_WORKFLOW) {
			String userScriptFilePath = job.getInputDirectory().toString() +
				File.separator + "user.ijm";

			String resourceFilePath = getClass().getClassLoader().getResource(
				"MacroWrapper.ijm").getPath();

			try (PrintWriter pw = new PrintWriter(job.getInputDirectory().toString() +
				File.separator + Constants.DEFAULT_MACRO_FILE))
			{

				// Write the MPI wrapper script's contents into the new script:
				copyLineByLine(pw, resourceFilePath);

				// Write user's script contents to the new script:
				copyLineByLine(pw, userScriptFilePath);

			}
			catch (FileNotFoundException exc) {
				log.error(exc.getMessage());
			}

			log.info(
				"Merged user's script and fiji macro MPI wrapper into mpitest.txt");
		}
	}

	private void copyLineByLine(PrintWriter pw, String filePath) {
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

			String line = br.readLine();

			// Copy line by line:
			while (line != null) {
				pw.println(line);
				line = br.readLine();
			}
		}
		catch (IOException exc) {
			log.error(exc.toString());
		}
	}

	private void deleteJob(BenchmarkJob bj) {
		bj.delete();
		jobs.getItems().remove(registry.remove(bj));
	}

	private void askForCreateJob() {
		NewJobWindow newJobWindow = new NewJobWindow(null);
		ModalDialogs.doModal(newJobWindow, WindowConstants.DISPOSE_ON_CLOSE);
		newJobWindow.setCreatePressedNotifier(() -> executeWSCallAsync(
			"Creating job", false, new P_JobAction()
			{

				@Override
				public void doAction(Progress p) throws IOException {
					BenchmarkJob job = doCreateJob(newJobWindow::getInputDirectory,
						newJobWindow::getOutputDirectory, newJobWindow.getNumberOfNodes(),
						newJobWindow.getHaasTemplateId());
					if (job.isUseDemoData()) {
						job.storeDataInWorkdirectory(getConfigYamlFile());
					}
					else if (job.getWorkflowType() == WorkflowType.SPIM_WORKFLOW && (job
						.getInputDirectory().resolve(CONFIG_YAML)).toFile().exists())
			{
				executorServiceFX.execute(() -> {

					Alert al = new Alert(AlertType.CONFIRMATION, "The file \"" +
						CONFIG_YAML + "\" found in the defined data input directory \"" +
						job.getInputDirectory() +
						"\". Would you like to copy it into the job working directory \"" +
						job.getDirectory() + "\"?", ButtonType.YES, ButtonType.NO);

					al.setHeaderText(null);
					al.setTitle("Copy " + CONFIG_YAML + "?");
					al.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
					if (al.showAndWait().get() == ButtonType.YES) {
						try {
							Files.copy(job.getInputDirectory().resolve(CONFIG_YAML), job
								.getDirectory().resolve(CONFIG_YAML));
						}
						catch (IOException e) {
							log.error(e.getMessage(), e);
						}
					}
				});

			}
				}
			}));

	}

	private UploadingFile getConfigYamlFile() {
		return new UploadingFileFromResource("", Constants.CONFIG_YAML);
	}

	private BenchmarkJob doCreateJob(UnaryOperator<Path> inputProvider,
		UnaryOperator<Path> outputProvider, int numberOfNodes, int haasTemplateId)
		throws IOException
	{
		BenchmarkJob bj = manager.createJob(inputProvider, outputProvider,
			numberOfNodes, haasTemplateId);
		ObservableBenchmarkJob obj = registry.addIfAbsent(bj);
		addJobToItems(obj);
		jobs.refresh();
		return bj;
	}

	private synchronized void addJobToItems(ObservableBenchmarkJob obj) {
		jobs.getItems().add(obj);
	}

	private void openJobSubdirectory(BenchmarkJob j) {
		executorServiceShell.execute(() -> {
			try {
				ShellRoutines.openDirectoryInBrowser(j.getDirectory());
			}
			catch (UnsupportedOperationException | IOException e) {
				// TODO: Escalate an error to the end user
				log.error(e.getMessage(), e);
			}
		});
	}

	private void executeWSCallAsync(String title, P_JobAction action) {
		executeWSCallAsync(title, true, action);
	}

	private void executeWSCallAsync(String title, boolean update,
		P_JobAction action)
	{
		JavaFXRoutines.executeAsync(executorServiceWS, (Callable<Void>) () -> {
			ProgressDialog dialog = ModalDialogs.doModal(new ProgressDialog(root,
				title), WindowConstants.DO_NOTHING_ON_CLOSE);
			try {
				action.doAction(dialog);
			}
			finally {
				dialog.done();
			}
			return null;
		}, x -> {
			if (update) {
				updateJobs(true);
			}
		});
	}

	private boolean checkConnection() {
		boolean[] result = { false };
		Progress progress = ModalDialogs.doModal(new ProgressDialog(root,
			"Connecting to HPC"), WindowConstants.DO_NOTHING_ON_CLOSE);
		final CountDownLatch latch = new CountDownLatch(1);
		executorServiceWS.execute(() -> {
			try {
				manager.checkConnection();
				result[0] = true;
			}
			finally {
				progress.done();
				latch.countDown();
			}
		});
		try {
			latch.await();
		}
		catch (InterruptedException exc) {
			log.error(exc.getMessage(), exc);
		}
		return result[0];
	}

	private void updateJobs(boolean showProgress) {
		Progress progress = showProgress ? ModalDialogs.doModal(new ProgressDialog(
			root, "Updating jobs"), WindowConstants.DO_NOTHING_ON_CLOSE)
			: new DummyProgress();

		executorServiceWS.execute(() -> {
			List<BenchmarkJob> inspectedJobs = new LinkedList<>(manager.getJobs());
			inspectedJobs.sort((bj1, bj2) -> (int) (bj1.getId() - bj2.getId()));
			for (BenchmarkJob bj : inspectedJobs) {
				registry.addIfAbsent(bj);
			}
			registry.update();
			Set<ObservableValue<BenchmarkJob>> actual = new HashSet<>(this.jobs
				.getItems());

			executorServiceFX.execute(() -> {
				for (ObservableBenchmarkJob value : registry.getAllItems()) {
					if (!actual.contains(value)) {
						addJobToItems(value);
					}
				}
			});
			progress.done();
		});
	}

	private void initTable() {
		registry = new ObservableBenchmarkJobRegistry(this::remove,
			executorServiceJobState, executorServiceFX);
		setCellValueFactory(0, j -> j.getId() + "");
		setCellValueFactoryCompletable(1, j -> j.getStateAsync(
			executorServiceJobState).thenApply(state -> "" + provider.getName(
				state)));
		setCellValueFactory(2, BenchmarkJob::getCreationTime);
		setCellValueFactory(3, BenchmarkJob::getStartTime);
		setCellValueFactory(4, BenchmarkJob::getEndTime);
		setCellValueFactory(5, j -> decorateTransfer(registry.get(j)
			.getUploadProgress()));
		setCellValueFactory(6, j -> decorateTransfer(registry.get(j)
			.getDownloadProgress()));
		setCellValueFactory(7, BenchmarkJob::getHaasTemplateName);
		JavaFXRoutines.setOnDoubleClickAction(jobs, executorServiceJobState,
			openJobDetailsWindow -> true, this::openJobDetailsWindow);
	}

	private String decorateTransfer(TransferProgress progress) {
		String stateMessage = "N/A";
		if (progress.isFailed()) {
			stateMessage = "Failed";
		}
		else if (!progress.isWorking() && !progress.isDone()) {
			stateMessage = "";
		}
		else if (progress.isWorking()) {
			Long msecs = progress.getRemainingMiliseconds();
			stateMessage = "Time remains " + (msecs != null ? RemainingTimeFormater
				.format(msecs) : "N/A");
		}
		else if (progress.isDone()) {
			stateMessage = "Done";
		}
		return stateMessage;
	}

	private void remove(BenchmarkJob bj) {
		jobs.getItems().remove(registry.get(bj));
	}

	private void setCellValueFactory(int index,
		Function<BenchmarkJob, String> mapper)
	{
		JavaFXRoutines.setCellValueFactory(jobs, index, mapper);
	}

	@SuppressWarnings("unchecked")
	private void setCellValueFactoryCompletable(int index,
		Function<BenchmarkJob, CompletableFuture<String>> mapper)
	{
		JavaFXRoutines.setCellValueFactory(jobs, index, mapper);
		((TableColumn<ObservableBenchmarkJob, CompletableFuture<String>>) jobs
			.getColumns().get(index)).setCellFactory(column -> new TableCellAdapter<> //
		(//
			new P_TableCellUpdaterDecoratorWithToolTip<>//
			(//
				new FutureValueUpdater<>//
				(//
					new StringValueUpdater<ObservableBenchmarkJob>(), executorServiceFX//
				), //
				"Doubleclick to open Dashboard")));
	}

	private void openJobDetailsWindow(ObservableBenchmarkJob job) {
		new JobDetailWindow(root, job).setVisible(true);
	}

	private void openBigDataViewer(BenchmarkJob job) {
		Path localPathToResultXML = job.getLocalPathToResultXML();
		String openFile;
		if (localPathToResultXML.toFile().exists()) {
			openFile = localPathToResultXML.toString();
		}
		else {
			openFile = job.getPathToToResultXMLOnBDS();
		}
		try {
			BigDataViewer.open(openFile, "Result of job " + job.getId(),
				new ProgressWriterConsole(), ViewerOptions.options());
		}
		catch (SpimDataException e) {
			log.error(e.getMessage(), e);
		}
	}

	private void openEditor(BenchmarkJob job) {
		TextEditor txt = new TextEditor(new Context()); // TODO Context handling is
																										// wrong
		File editFile = new File(job.getInputDirectory().toString(),
			Constants.DEFAULT_MACRO_FILE);
		txt.open(editFile);
		txt.setVisible(true);
	}

	private interface P_JobAction {

		public void doAction(Progress p) throws IOException;
	}

	private class P_TableCellUpdaterDecoratorWithToolTip<S, T> implements
		TableCellUpdater<S, T>
	{

		private final TableCellUpdater<S, T> decorated;

		private final String toolTipText;

		public P_TableCellUpdaterDecoratorWithToolTip(
			TableCellUpdater<S, T> decorated, String toolTipText)
		{
			this.decorated = decorated;
			this.toolTipText = toolTipText;
		}

		@Override
		public void accept(TableCell<?, ?> cell, T value, boolean empty) {
			decorated.accept(cell, value, empty);
			cell.setTooltip(new Tooltip(toolTipText));
		}

	}
}
