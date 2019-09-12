
package cz.it4i.fiji.hpc_workflow.commands;

import net.imagej.ImageJ;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.fiji.hpc_workflow.ui.LoginViewWindow;
import groovy.util.logging.Slf4j;

@Slf4j
@Plugin(type = Command.class, headless = false, priority = Priority.HIGH,
	menuPath = "Plugins>" + Constants.MENU_ITEM_NAME + ">" +
		Constants.SUBMENU_ITEM_NAME)
public class NewManageHPCWorkflow implements Command {

	private static Logger log = LoggerFactory.getLogger(ManageHPCWorkflow.class);

	@Parameter
	private Context context;

	@Override
	public void run() {
		// Display window:
		LoginViewWindow loginViewWindow = new LoginViewWindow(null);
		JavaFXRoutines.runOnFxThread(() -> {
			loginViewWindow.initialize(context.getService(PrefService.class));
			loginViewWindow.openWindow();
			loginViewWindow.startJobDetailIfPossible();
		});
	}

	public static void main(final String... args) {
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.launch(args);
		if (log.isDebugEnabled()) {
			log.debug("run NewManageHPCWorkflow");
		}
		ij.command().run(NewManageHPCWorkflow.class, true);
	}
}
