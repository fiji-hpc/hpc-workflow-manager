
package cz.it4i.fiji.hpc_workflow.commands;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.command.Command;
import org.scijava.parallel.ParallelService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import cz.it4i.fiji.hpc_workflow.core.Constants;

// This calls the ManageHPCWorkflow class.

@Plugin(type = Command.class, headless = false, priority = Priority.HIGH,
	menuPath = "Plugins>" + Constants.ALTERNATIVE_MENU_ITEM_NAME + ">" +
		Constants.ALTERNATIVE_SUBMENU_ITEM_NAME)
public class ParallelMacroClient implements Command {

	@Parameter
	private Context context;

	@Parameter
	private ParallelService parallelService;

	@Override
	public void run() {
		ManageHPCWorkflow mhw = new ManageHPCWorkflow();
		mhw.runWithGivenParameterValues(this.context, this.parallelService);
	}

}
