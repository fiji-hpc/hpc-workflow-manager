package cz.it4i.fiji.haas_spim_benchmark.commands;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import cz.it4i.fiji.haas_spim_benchmark.ui.BenchmarkSPIMWindow;

/**
 * 
 * @author koz01
 *
 */
@Plugin(type = Command.class, headless = false, menuPath = "Plugins>" + Constants.MENU_ITEM_NAME + ">" + Constants.SUBMENU_ITEM_NAME)
public class ManageSPIMBenchmark implements Command {

	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.commands.ManageSPIMBenchmark.class);
	
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

	@Parameter(label = "Working directory", persist = true, style = FileWidget.DIRECTORY_STYLE)
	private File workingDirectory;
	
	@Override
	public void run() {
		try {
			final Path workingDirPath = Paths.get(workingDirectory.getPath());
			if (!Files.isDirectory(workingDirPath)) {
				Files.createDirectories(workingDirPath);
			}
			@SuppressWarnings("resource")
			final FileLock fl = new FileLock(workingDirPath.resolve(LOCK_FILE_NAME));
			if(!fl.tryLock()) {
				uiService.showDialog("Working directory is already used by someone else", MessageType.ERROR_MESSAGE);
				return;
			}
			final BenchmarkSPIMWindow dialog = new BenchmarkSPIMWindow(null,
					new BenchmarkSPIMParametersImpl(userName, password, Constants.PHONE, email, workingDirPath));
			dialog.executeAdjustment(() -> {
				dialog.setTitle(Constants.SUBMENU_ITEM_NAME);
				dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				dialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(final WindowEvent e) {
						super.windowClosing(e);
						fl.close();
					}
				});
				dialog.setVisible(true);
			});
		} catch (final IOException e) {
			log.error(e.getMessage(), e);
		}

	}

	public static void main(final String... args) {
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.launch(args);
		ij.command().run(ManageSPIMBenchmark.class, true);
	}

}
