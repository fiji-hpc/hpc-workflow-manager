
package cz.it4i.fiji.hpc_workflow.commands;

import net.imagej.ImageJ;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.command.Command;
import org.scijava.parallel.ParallelService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.hpc_workflow.WorkflowParadigm;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.fiji.hpc_workflow.ui.HPCWorkflowWindow;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
import groovy.util.logging.Slf4j;
import javafx.application.Platform;

@Slf4j
@Plugin(type = Command.class, headless = false, priority = Priority.HIGH,
	menuPath = "Plugins>" + Constants.MENU_ITEM_NAME + ">" +
		Constants.SUBMENU_ITEM_NAME)
public class ManageHPCWorkflow implements Command {

	private static Logger log = LoggerFactory.getLogger(ManageHPCWorkflow.class);

	@Parameter
	private Context context;

	@Parameter
	private ParallelService parallelService;

	@Override
	public void run() {
		// Display window:
		WorkflowParadigm paradigm = parallelService.getParadigmOfType(
			WorkflowParadigm.class);
		if (paradigm != null) {
			JavaFXRoutines.runOnFxThread(() -> new HPCWorkflowWindow().openWindow(
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

	public static void main(final String... args) {
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.launch(args);
		if (log.isDebugEnabled()) {
			log.debug("run NewManageHPCWorkflow");
		}
		ij.command().run(ManageHPCWorkflow.class, true);
	}
}
