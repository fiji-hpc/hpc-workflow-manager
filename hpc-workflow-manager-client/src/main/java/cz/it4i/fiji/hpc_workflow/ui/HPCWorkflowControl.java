
package cz.it4i.fiji.hpc_workflow.ui;

import static cz.it4i.fiji.hpc_workflow.core.Configuration.getHaasUpdateTimeout;
import static cz.it4i.fiji.hpc_workflow.core.Constants.CONFIG_YAML;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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

import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.scijava.Context;
import org.scijava.parallel.Status;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;
import org.scijava.ui.swing.script.TextEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.BigDataViewer;
import bdv.export.ProgressWriterConsole;
import bdv.viewer.ViewerOptions;
import cz.it4i.fiji.hpc_adapter.JobWithDirectorySettings;
import cz.it4i.fiji.hpc_adapter.ui.FutureValueUpdater;
import cz.it4i.fiji.hpc_adapter.ui.ShellRoutines;
import cz.it4i.fiji.hpc_adapter.ui.StringValueUpdater;
import cz.it4i.fiji.hpc_adapter.ui.TableCellAdapter;
import cz.it4i.fiji.hpc_adapter.ui.TableCellAdapter.TableCellUpdater;
import cz.it4i.fiji.hpc_adapter.ui.TableViewContextMenu;
import cz.it4i.fiji.hpc_client.JobState;
import cz.it4i.fiji.hpc_client.ProgressNotifier;
import cz.it4i.fiji.hpc_workflow.WorkflowJob;
import cz.it4i.fiji.hpc_workflow.WorkflowParadigm;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.fiji.hpc_workflow.core.FXFrameExecutorService;
import cz.it4i.fiji.hpc_workflow.core.MacroJob;
import cz.it4i.fiji.hpc_workflow.core.ObservableHPCWorkflowJob;
import cz.it4i.fiji.hpc_workflow.core.ObservableHPCWorkflowJob.TransferProgress;
import cz.it4i.fiji.hpc_workflow.core.JobType;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
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
import javafx.stage.Stage;
import mpicbg.spim.data.SpimDataException;

public class HPCWorkflowControl<T extends JobWithDirectorySettings> extends
	BorderPane
{

	@Parameter
	private PluginService pluginService;

	@Parameter
	private Context context;

	@FXML
	private TableView<ObservableHPCWorkflowJob> jobs;

	private final WorkflowParadigm<T> paradigm;

	private final ExecutorService executorServiceJobState = Executors
		.newWorkStealingPool();

	private final Executor executorServiceFX = new FXFrameExecutorService();

	private ExecutorService executorServiceShell;

	private ExecutorService executorServiceWS;

	private Timer timer;

	private ObservableHPCWorkflowJobRegistry registry;

	private final JobStateNameProvider provider = new JobStateNameProvider();

	private boolean closed;

	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_workflow.ui.HPCWorkflowControl.class);

	private Stage stage;

	public HPCWorkflowControl(WorkflowParadigm<T> paradigm) {
		this.paradigm = paradigm;
		JavaFXRoutines.initRootAndController("HPCWorkflow.fxml", this);
		jobs.setPlaceholder(new Label(
			"No jobs in table. Right click to create new job."));
	}

	public CompletableFuture<Void> init(Stage newStage) {
		this.stage = newStage;

		executorServiceWS = Executors.newSingleThreadExecutor();
		executorServiceShell = Executors.newSingleThreadExecutor();
		timer = new Timer();
		initTable();
		initMenu();
		return initParadigm().thenAccept(this::startUpdater);
	}

	private synchronized void startUpdater(@SuppressWarnings("unused") Void x) {
		if (paradigm.getStatus() == Status.ACTIVE && !closed) {
			timer.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					updateJobs(false);
				}
			}, getHaasUpdateTimeout(), getHaasUpdateTimeout());
			updateJobs(true);
		}
	}

	public synchronized void close() {
		if (!closed) {
			executorServiceShell.shutdown();
			executorServiceWS.shutdown();
			executorServiceJobState.shutdown();
			registry.close();
			timer.cancel();
			paradigm.close();
			closed = true;
		}
	}

	private void initMenu() {
		TableViewContextMenu<ObservableHPCWorkflowJob> menu =
			new TableViewContextMenu<>(jobs);
		menu.addItem("Create a new job", x -> askForCreateJob(), j -> true,
			MaterialDesign.MDI_CREATION);
		menu.addSeparator();

		menu.addItem("Start job", job -> {
			JobType jobType = job.getJobType();
			if (jobType == JobType.MACRO || jobType == JobType.SCRIPT) job
				.setLastStartedTimestamp();
			executeWSCallAsync("Starting job", p -> {
				job.getValue().startJob(p);
				job.getValue().update();
			});
		}, job -> JavaFXRoutines.notNullValue(job, j -> {
			JobState jobState = j.getState();
			return Arrays.asList(JobState.Configuring, JobState.Finished,
				JobState.Failed, JobState.Canceled).contains(jobState) &&
				hasAnythingBeenUploaded(j);
		}), MaterialDesign.MDI_PLAY);

		menu.addItem("Cancel job", job -> executeWSCallAsync("Canceling job", p -> {
			job.getValue().cancelJob();
			job.getValue().update();
		}), job -> JavaFXRoutines.notNullValue(job, j -> {
			JobState jobState = j.getState();
			return (jobState == JobState.Running || jobState == JobState.Queued);
		}), MaterialDesign.MDI_STOP);

		menu.addItem("Job dashboard", this::openJobDetailsWindow,
			job -> JavaFXRoutines.notNullValue(job, j -> true),
			MaterialDesign.MDI_VIEW_DASHBOARD);
		menu.addItem("Open job subdirectory", j -> openJobSubdirectory(j
			.getValue()), x -> JavaFXRoutines.notNullValue(x, j -> true),
			MaterialDesign.MDI_FOLDER);
		menu.addItem("Open in BigDataViewer", j -> openBigDataViewer(j.getValue()),
			x -> JavaFXRoutines.notNullValue(x, j -> j
				.getState() == JobState.Finished && j.isVisibleInBDV()),
			MaterialDesign.MDI_EYE);
		menu.addItem("Open in editor", j -> openEditor(j.getValue(), this.context),
			x -> JavaFXRoutines.notNullValue(x, j -> {
				JobType jobType = j.getJobType();
				return jobType == JobType.MACRO || jobType == JobType.SCRIPT;
			}), MaterialDesign.MDI_LEAD_PENCIL);

		menu.addSeparator();

		menu.addItem("Upload data", job -> {
			boolean wasSuccessfull = createTheMacroScript(job);
			if (wasSuccessfull) executeWSCallAsync("Uploading data", p -> job
				.getValue().startUpload());
		}, job -> executeWSCallAsync("Stop uploading data", p -> job.getValue()
			.stopUpload()), job -> JavaFXRoutines.notNullValue(job, j -> j
				.canBeUploaded() && !EnumSet.of(JobState.Running, JobState.Disposed)
					.contains(j.getState())), job -> job != null && job
						.getUploadProgress().isWorking(), MaterialDesign.MDI_UPLOAD);

		menu.addItem("Download result", job -> executeWSCallAsync(
			"Downloading data", p -> job.getValue().startDownload()),
			job -> executeWSCallAsync("Stop downloading data", p -> job.getValue()
				.stopDownload()), job -> JavaFXRoutines.notNullValue(job, j -> EnumSet
					.of(JobState.Failed, JobState.Finished, JobState.Canceled).contains(j
						.getState()) && j.canBeDownloaded()), job -> job != null && job
							.getDownloadProgress().isWorking(), MaterialDesign.MDI_DOWNLOAD);

		menu.addItem("Explore errors", job -> job.getValue().exploreErrors(),
			job -> JavaFXRoutines.notNullValue(job, j -> j.getState().equals(
				JobState.Failed)), MaterialDesign.MDI_ALERT_CIRCLE);

		menu.addSeparator();

		menu.addItem("Delete job", j -> {
			if (confirmDelete()) {
				deleteJob(j.getValue());
			}
		}, x -> JavaFXRoutines.notNullValue(x, j -> canDelete(j.getState())),
			MaterialDesign.MDI_DELETE);

	}

	// Ask for confirmation in order to delete the job:
	private boolean confirmDelete() {
		return SimpleDialog.showConfirmation("Delete job?",
			"Are you sure you want to delete this job?");
	}

	// In order to be able to delete a job it should be
	// done one way or another (Cancelled, Disposed, Failed, Finished or
	// Configured).
	private boolean canDelete(JobState jobState) {
		return Arrays.asList(JobState.Canceled, JobState.Disposed, JobState.Failed,
			JobState.Finished, JobState.Configuring).contains(jobState);
		// If false then the job must be cancelled first.
	}

	private boolean hasAnythingBeenUploaded(WorkflowJob job) {
		// If it is not a Macro Workflow then this method should have no impact
		// therefore it returns true:
		boolean uploaded = true;
		if (Arrays.asList(JobType.MACRO, JobType.SCRIPT).contains(job
			.getJobType()))
		{
			// If the user has not uploaded anything return false:
			try {
				if (!job.isUploaded()) {
					uploaded = false;
				}
			}
			catch (Exception exc) {
				uploaded = false;
			}
		}
		return uploaded;
	}

	private boolean createTheMacroScript(ObservableHPCWorkflowJob job) {
		boolean isSuccessfull = true;
		if (job.getJobType() == JobType.MACRO) {
			String userScriptFilePath = job.getInputDirectory().toString() +
				File.separator + job.getUserScriptName();

			// Remove old wrapped script file if one exists:
			String parallelMacroWrappedString = job.getInputDirectory().toString() +
				File.separator + Constants.DEFAULT_MACRO_FILE;
			File parallelMacroWrappedFile = new File(parallelMacroWrappedString);
			try {
				Files.deleteIfExists(parallelMacroWrappedFile.toPath());
			}
			catch (IOException exc) {
				SimpleDialog.showException("Exception",
					"Could not delete the old wrapped Macro user script.", exc);
			}

			// Create the new wrapped script file:
			try (BufferedReader resourceReader = new BufferedReader(
				new InputStreamReader(HPCWorkflowControl.class.getClassLoader()
					.getResourceAsStream("MacroWrapper.ijm")));
					PrintWriter pw = new PrintWriter(job.getInputDirectory().toString() +
						File.separator + Constants.DEFAULT_MACRO_FILE))
			{
				// Write user's script contents to the new script:
				isSuccessfull = copyLineByLine(pw, userScriptFilePath);

				// Write the MPI wrapper script's contents into the new script:
				isSuccessfull = isSuccessfull && copyLineByLine(pw, resourceReader);
			}
			catch (FileNotFoundException exc) {
				log.error(exc.getMessage());
				isSuccessfull = false;
			}
			catch (IOException exc) {
				log.error(exc.getMessage());
			}

			log.info(
				"Merged user's script and fiji macro MPI wrapper into new file: " +
					Constants.DEFAULT_MACRO_FILE);
		}
		return isSuccessfull;
	}

	private boolean copyLineByLine(PrintWriter pw,
		BufferedReader resourceReader)
	{
		try {
			String line = resourceReader.readLine();

			// Copy line by line:
			while (line != null) {
				pw.print(line);
				// Use the second print to add a Linux format new line, if println is
				// used and the client is running on Windows it will add incorrect
				// newline:
				pw.print("\n");
				line = resourceReader.readLine();
			}

		}
		catch (IOException exc) {
			log.error(exc.toString());
			return false;
		}
		return true;
	}

	private boolean copyLineByLine(PrintWriter pw, String filePath) {
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			return copyLineByLine(pw, br);
		}
		catch (IOException exc) {
			log.error(exc.toString());
			return false;
		}
	}

	private void deleteJob(WorkflowJob bj) {
		bj.delete();
		jobs.getItems().remove(registry.remove(bj));
	}

	private void askForCreateJob() {
		@SuppressWarnings("unchecked")
		JavaFXJobSettingsProvider<T> settingsProvider = pluginService
			.createInstancesOfType(JavaFXJobSettingsProvider.class).stream().filter(
				p -> p.getTypeOfJobSettings().equals(paradigm.getTypeOfJobSettings()))
			.findFirst().orElse(null);
		if (settingsProvider == null) {
			return;
		}

		settingsProvider.provideJobSettings(stage,
			jobSettings -> executeWSCallAsync("Creating job", false,
				notifier -> doJobAction(notifier, jobSettings)));
	}

	private void doJobAction(
		@SuppressWarnings("unused") ProgressNotifier notifier, T jobSettings)
		throws IOException
	{
		WorkflowJob job = doCreateJob(jobSettings);

		if (jobSettings.getInputPath().apply(job.getDirectory()) != null && job
			.getJobType() == JobType.SPIM_WORKFLOW && (job.getInputDirectory()
				.resolve(CONFIG_YAML)).toFile().exists())
		{
			executorServiceFX.execute(() -> {

				Alert al = new Alert(AlertType.CONFIRMATION, "The file \"" +
					CONFIG_YAML + "\" found in the defined data input directory \"" + job
						.getInputDirectory() +
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
						SimpleDialog.showException("Exception",
							"Exception occurred while trying to create job.", e);
					}
				}
			});

		}
	}

	private WorkflowJob doCreateJob(T settings) throws IOException {
		WorkflowJob bj;
		bj = paradigm.createJob(settings);
		ObservableHPCWorkflowJob obj = registry.addIfAbsent(bj);
		addJobToItems(obj);
		jobs.refresh();
		return bj;
	}

	private synchronized void addJobToItems(ObservableHPCWorkflowJob obj) {
		jobs.getItems().add(obj);
	}

	private void openJobSubdirectory(WorkflowJob j) {
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

	private void executeWSCallAsync(String title, PJobAction action) {
		executeWSCallAsync(title, true, action);
	}

	private void executeWSCallAsync(String title, boolean update,
		PJobAction action)
	{
		JavaFXRoutines.executeAsync(executorServiceWS, (Callable<Void>) () -> {
			ProgressDialogViewWindow progress = new ProgressDialogViewWindow(title,
				this.stage);
			try {
				action.doAction(progress);
			}
			finally {
				progress.done();
			}
			return null;
		}, x -> {
			if (update) {
				updateJobs(true);
			}
		});
	}

	private CompletableFuture<Void> initParadigm() {

		if (paradigm.getStatus() == Status.ACTIVE) {
			return CompletableFuture.completedFuture(null);
		}
		return CompletableFuture.runAsync(paradigm::init, executorServiceWS);
	}

	private void updateJobs(boolean showProgress) {
		executorServiceWS.execute(() -> {
			ProgressDialogViewWindow progress = new ProgressDialogViewWindow(
				"Updating jobs", this.stage, showProgress);
			List<WorkflowJob> inspectedJobs = new LinkedList<>(paradigm.getJobs());
			inspectedJobs.sort((j1, j2) -> (int) (j1.getId() - j2.getId()));
			for (WorkflowJob bj : inspectedJobs) {
				registry.addIfAbsent(bj);
			}
			registry.update();
			Set<ObservableValue<WorkflowJob>> actual = new HashSet<>(this.jobs
				.getItems());

			executorServiceFX.execute(() -> {
				for (ObservableHPCWorkflowJob value : registry.getAllItems()) {
					if (!actual.contains(value)) {
						addJobToItems(value);
					}
				}
			});
			progress.done();
		});
	}

	private void initTable() {
		registry = new ObservableHPCWorkflowJobRegistry(this::remove,
			executorServiceJobState, executorServiceFX);
		setCellValueFactory(0, j -> j.getId() + "");
		setCellValueFactoryCompletable(1, j -> j.getStateAsync(
			executorServiceJobState).thenApply(state -> "" + provider.getName(
				state)));
		setCellValueFactory(2, WorkflowJob::getCreationTime);
		setCellValueFactory(3, WorkflowJob::getStartTime);
		setCellValueFactory(4, WorkflowJob::getEndTime);
		setCellValueFactory(5, j -> decorateTransfer(registry.get(j)
			.getUploadProgress()));
		setCellValueFactory(6, j -> decorateTransfer(registry.get(j)
			.getDownloadProgress()));
		setCellValueFactory(7, WorkflowJob::getJobTypeName);
		JavaFXRoutines.setOnDoubleClickAction(jobs, executorServiceJobState,
			openJobDetailsWindow -> true, this::openJobDetailsWindow);
	}

	private String decorateTransfer(TransferProgress progress) {
		String stateMessage;
		boolean isWorking = progress.isWorking();
		boolean isDone = progress.isDone();
		boolean isFailed = progress.isFailed();
		if (isFailed) {
			stateMessage = "Failed";
		}
		else if (isDone) {
			stateMessage = "Done";
		}
		else if (isWorking) {
			Long msecs = progress.getRemainingMiliseconds();
			stateMessage = "Time remains " + (msecs != null ? RemainingTimeFormater
				.format(msecs) : "N/A");
		}
		else {
			stateMessage = "";
		}
		return stateMessage;
	}

	private void remove(WorkflowJob bj) {
		jobs.getItems().remove(registry.get(bj));
	}

	private void setCellValueFactory(int index,
		Function<WorkflowJob, String> mapper)
	{
		JavaFXRoutines.setCellValueFactory(jobs, index, mapper);
	}

	@SuppressWarnings("unchecked")
	private void setCellValueFactoryCompletable(int index,
		Function<WorkflowJob, CompletableFuture<String>> mapper)
	{
		JavaFXRoutines.setCellValueFactory(jobs, index, mapper);
		((TableColumn<ObservableHPCWorkflowJob, CompletableFuture<String>>) jobs
			.getColumns().get(index)).setCellFactory(column -> new TableCellAdapter<> //
		(//
			new PTableCellUpdaterDecoratorWithToolTip<>//
			(//
				new FutureValueUpdater<>//
				(//
					new StringValueUpdater(), executorServiceFX//
				), //
				"Doubleclick to open Dashboard")));
	}

	private void openJobDetailsWindow(ObservableHPCWorkflowJob job) {
		new JobDetailWindow(job);
	}

	private void openBigDataViewer(WorkflowJob job) {
		Path localPathToResultXML = job.getPathToLocalResultFile();
		String openFile;
		if (localPathToResultXML.toFile().exists()) {
			openFile = localPathToResultXML.toString();
		}
		else {
			openFile = job.getPathToRemoteResultFile();
		}
		try {
			BigDataViewer.open(openFile, "Result of job " + job.getId(),
				new ProgressWriterConsole(), ViewerOptions.options());
		}
		catch (SpimDataException e) {
			log.error(e.getMessage(), e);
		}
	}

	private void openEditor(WorkflowJob job, Context givenContext) {
		if (job instanceof MacroJob) {
			MacroJob typeJob = (MacroJob) job;
			// Open the user script:
			String userScriptPathString = typeJob.getInputDirectory().toString() +
				File.separator + typeJob.getUserScriptName();
			File editFile = new File(userScriptPathString);
			if (editFile.isFile()) {
				// Open the text editor (AWT based):
				EventQueue.invokeLater(() -> {
					TextEditor txt = new TextEditor(givenContext);
					txt.open(editFile);
					txt.setVisible(true);
					txt.toFront();
				});
			}
			else {
				JavaFXRoutines.runOnFxThread(() -> SimpleDialog.showInformation(
					"Script file is missing.",
					"You may have moved, removed or renamed the script file and it can no longer be found at the following location:\n" +
						userScriptPathString));
			}
		}
	}

	private interface PJobAction {

		public void doAction(ProgressNotifier progress) throws IOException;
	}

	private static class PTableCellUpdaterDecoratorWithToolTip<S, T> implements
		TableCellUpdater<T>
	{

		private final TableCellUpdater<T> decorated;

		private final String toolTipText;

		public PTableCellUpdaterDecoratorWithToolTip(TableCellUpdater<T> decorated,
			String toolTipText)
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
