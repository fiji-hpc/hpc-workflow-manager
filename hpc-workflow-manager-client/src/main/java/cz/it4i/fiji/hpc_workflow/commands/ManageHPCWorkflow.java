
package cz.it4i.fiji.hpc_workflow.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.WindowConstants;

import net.imagej.ImageJ;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt.MessageType;
import org.scijava.ui.UIService;
import org.scijava.widget.FileWidget;
import org.scijava.widget.TextWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.commons.UncaughtExceptionHandlerDecorator;
import cz.it4i.fiji.hpc_workflow.core.AuthFailExceptionHandler;
import cz.it4i.fiji.hpc_workflow.core.AuthenticationExceptionHandler;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.fiji.hpc_workflow.core.NotConnectedExceptionHandler;
import cz.it4i.fiji.hpc_workflow.core.WindowCloseableAdapter;
import cz.it4i.fiji.hpc_workflow.ui.HPCWorkflowWindow;

/**
 * @author koz01
 */
@Plugin(type = Command.class, headless = false, menuPath = "Plugins>" +
	Constants.MENU_ITEM_NAME + ">" + Constants.SUBMENU_ITEM_NAME)
public class ManageHPCWorkflow implements Command {

	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_workflow.commands.ManageHPCWorkflow.class);

	private static final String LOCK_FILE_NAME = ".lock";

	@Parameter
	private UIService uiService;

	@Parameter
	private Context context;

	@Parameter(style = TextWidget.FIELD_STYLE, label = "User name")
	private String userName;

	@Parameter(style = TextWidget.PASSWORD_STYLE)
	private String password;

	@Parameter(style = TextWidget.FIELD_STYLE)
	private String email;

	@Parameter(label = "Working directory", persist = true,
		style = FileWidget.DIRECTORY_STYLE)
	private File workingDirectory;

	@Override
	public void run() {
		if (log.isDebugEnabled()) {
			log.debug("DefaultUncaughtExceptionHandler() = {} ", Thread
				.getDefaultUncaughtExceptionHandler());
		}

		final UncaughtExceptionHandlerDecorator uehd =
			UncaughtExceptionHandlerDecorator.setDefaultHandler();
		final WindowCloseableAdapter wca = new WindowCloseableAdapter();
		uehd.registerHandler(new AuthenticationExceptionHandler(wca));
		uehd.registerHandler(new NotConnectedExceptionHandler(wca));
		uehd.registerHandler(new AuthFailExceptionHandler());
		uehd.activate();
		try {
			final Path workingDirPath = Paths.get(workingDirectory.getPath());
			if (!workingDirPath.toFile().isDirectory()) {
				Files.createDirectories(workingDirPath);
			}
			final FileLock fl = new FileLock(workingDirPath.resolve(LOCK_FILE_NAME));
			if (!fl.tryLock()) {
				uiService.showDialog(
					"Working directory is already used by someone else",
					MessageType.ERROR_MESSAGE);
				return;
			}

			final HPCWorkflowWindow dialog = new HPCWorkflowWindow(null,
				new HPCWorkflowParametersImpl(userName, password, Constants.PHONE,
					email, workingDirPath))
			{

				@Override
				public void dispose() {
					super.dispose();
					fl.close();
					uehd.close();
				}
			};

			dialog.executeAdjustment(() -> {
				dialog.setTitle(Constants.SUBMENU_ITEM_NAME);
				dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				wca.setWindowAndShowIt(dialog);
			});
		}
		catch (final IOException e) {
			log.error(e.getMessage(), e);
		}

	}

	public static void main(final String... args) {
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.launch(args);
		if (log.isDebugEnabled()) {
			log.debug("run ManageSPIMBenchmark");
		}
		ij.command().run(ManageHPCWorkflow.class, true);
	}

}
