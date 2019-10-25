
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
import groovy.util.logging.Slf4j;

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
		HPCWorkflowWindow.openWindow(paradigm);

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
