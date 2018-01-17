package cz.it4i.fiji.haas_snakemake_spim.commands;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.WindowConstants;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.ApplicationFrame;
import org.scijava.ui.UIService;
import org.scijava.widget.UIComponent;

import cz.it4i.fiji.haas.JobManager;
import cz.it4i.fiji.haas.TestingConstants;
import cz.it4i.fiji.haas.ui.ModalDialogs;
import cz.it4i.fiji.haas.ui.ProgressDialog;
import cz.it4i.fiji.haas_java_client.HaaSClient;
import net.imagej.ImageJ;

/**
 * 
 * @author koz01
 *
 */
@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Run with HaaS")
public class RunWithHaaS implements Command {

	@Parameter
	private UIService uiService;

	@Parameter
	private LogService log;

	@Parameter(label = "Work directory", persist = true, style = "directory")
	private File workDirectory;

	@Parameter(label = "Data directory", persist = true, style = "directory")
	private File dataDirectory;

	@Parameter
	private Context context;

	private JobManager jobManager;

	@Override
	public void run() {
		try {
			jobManager = new JobManager(getWorkingDirectoryPath(), TestingConstants.getSettings());
			jobManager.startJob(
					getContent(dataDirectory).stream().map(HaaSClient::getUploadingFile).collect(Collectors.toList()),
					ModalDialogs.doModal(new ProgressDialog(getFrame()), WindowConstants.DO_NOTHING_ON_CLOSE));
		} catch (IOException e) {
			log.error(e);
		}
	}

	private Path getWorkingDirectoryPath() {
		return Paths.get(workDirectory.toString());
	}

	private Collection<Path> getContent(File dataDirectory) throws IOException {
		return Files.list(Paths.get(dataDirectory.toString())).collect(Collectors.toList());
	}

	private Frame getFrame() {
		ApplicationFrame af = uiService.getDefaultUI().getApplicationFrame();
		if (af instanceof Frame) {
			return (Frame) af;
		} else if (af instanceof UIComponent) {
			Object component = ((UIComponent<?>) af).getComponent();
			if (component instanceof Frame) {
				return (Frame) component;
			}
		}
		return null;
	}

	public static void main(final String... args) {
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.launch(args);

		ij.command().run(RunWithHaaS.class, true);
	}

}
