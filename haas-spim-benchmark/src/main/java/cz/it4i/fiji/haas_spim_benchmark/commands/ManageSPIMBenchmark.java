package cz.it4i.fiji.haas_spim_benchmark.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.widget.FileWidget;
import org.scijava.widget.TextWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import cz.it4i.fiji.haas_spim_benchmark.ui.BenchmarkSPIMWindow;
import net.imagej.ImageJ;

/**
 * 
 * @author koz01
 *
 */
@Plugin(type = Command.class, headless = false, menuPath = "Plugins>SPIM benchmark")
public class ManageSPIMBenchmark implements Command {

	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.commands.ManageSPIMBenchmark.class);

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
			JDialog dialog = 
					new BenchmarkSPIMWindow(null, new BenchmarkSPIMParametersImpl(userName, password, Constants.PHONE,
							email, Paths.get(workingDirectory.getPath())));
			dialog.setTitle("SPIM workflow computation manager");
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
