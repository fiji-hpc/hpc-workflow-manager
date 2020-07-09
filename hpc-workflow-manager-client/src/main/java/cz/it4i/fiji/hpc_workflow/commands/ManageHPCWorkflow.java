
package cz.it4i.fiji.hpc_workflow.commands;

import net.imagej.ImageJ;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.command.Command;
import org.scijava.parallel.ParallelService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import cz.it4i.fiji.hpc_adapter.JobWithDirectorySettings;
import cz.it4i.fiji.hpc_workflow.WorkflowParadigm;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.fiji.hpc_workflow.ui.HPCWorkflowWindow;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Plugin(type = Command.class, headless = false, priority = Priority.HIGH,
	menuPath = "Plugins>" + Constants.MENU_ITEM_NAME + ">" +
		Constants.SUBMENU_ITEM_NAME)
public class ManageHPCWorkflow implements Command {


	@Parameter
	private Context context;

	@Parameter
	private ParallelService parallelService;

	@Override
	public void run() {
		runWithGivenParameterValues(this.context, this.parallelService);
	}

	public void runWithGivenParameterValues(Context givenContext,
		ParallelService givenParallelService)
	{
		// Display window:
		WorkflowParadigm<?> paradigm = givenParallelService.getParadigmOfType(
			WorkflowParadigm.class);
		if (paradigm != null) {
			JavaFXRoutines.runOnFxThread(() -> this.openWorkflowWindow(givenContext,
				paradigm));
		}
		else {
			JavaFXRoutines.runOnFxThread(() -> {
				Platform.setImplicitExit(false); // Do not stop the JavaFX thread.
				SimpleDialog.showWarning("There is no active workflow paradigm.",
					"Please start a workflow paradigm first!\nFrom the Fiji menu select:\n" +
						"\"Plugins > SciJava Parallel > Paradigm Profiles Manager\"\n" +
						"then activate an existing worklow paradigm or create a " +
						"new one and try running HPC Workflow Manager again.");
			});
		}
	}

	private <T extends JobWithDirectorySettings> void openWorkflowWindow(
		Context givenContext, WorkflowParadigm<?> paradigm)
	{
		@SuppressWarnings("unchecked")
		WorkflowParadigm<T> typedParadigm = (WorkflowParadigm<T>) paradigm;
		inject(givenContext, new HPCWorkflowWindow()).openWindow(typedParadigm);
	}

	private <T> T inject(Context givenContext, T toInject) {
		givenContext.inject(toInject);
		return toInject;
	}

	public static void main(final String... args) {
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.launch(args);
		log.debug("run NewManageHPCWorkflow");
		ij.command().run(ManageHPCWorkflow.class, true);
	}
}
