
package cz.it4i.fiji.hpc_workflow.commands;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.fiji.hpc_workflow.ui.AboutWindow;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;

// Show a list of useful links:

@Plugin(type = Command.class, headless = false, priority = Priority.HIGH,
	menuPath = "Plugins>" + Constants.MENU_ITEM_NAME + ">" + "About")
public class AboutCommand implements Command {

	@Parameter
	private Context context;

	@Override
	public void run() {
		JavaFXRoutines.runOnFxThread(() -> {
			AboutWindow aboutWindow = new AboutWindow();
			aboutWindow.openWindow();
		});
	}

}
