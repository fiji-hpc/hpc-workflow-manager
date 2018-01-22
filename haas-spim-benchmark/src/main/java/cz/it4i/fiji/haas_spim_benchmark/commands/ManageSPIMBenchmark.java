package cz.it4i.fiji.haas_spim_benchmark.commands;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.swing.WindowConstants;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.ApplicationFrame;
import org.scijava.ui.UIService;
import org.scijava.widget.FileWidget;
import org.scijava.widget.TextWidget;
import org.scijava.widget.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.ModalDialogs;
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
	
	@Parameter(style = TextWidget.FIELD_STYLE)
	private String userName;
	
	@Parameter(style = TextWidget.PASSWORD_STYLE)
	private String password;
	
	@Parameter(style = TextWidget.FIELD_STYLE)
	private String phone;
	
	@Parameter(style = TextWidget.FIELD_STYLE)
	private String email;

	@Parameter(label = "Work directory", persist = true, style = FileWidget.DIRECTORY_STYLE)
	private File workDirectory;
	
	@Override
	public void run() {
		try {
			ModalDialogs.doModal(
					new BenchmarkSPIMWindow(getFrame(), new BenchmarkSPIMParametersImpl(
							userName, password, phone, email, Paths.get(workDirectory.getPath()))), WindowConstants.DISPOSE_ON_CLOSE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
		}

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

		ij.command().run(ManageSPIMBenchmark.class, true);
	}

}
