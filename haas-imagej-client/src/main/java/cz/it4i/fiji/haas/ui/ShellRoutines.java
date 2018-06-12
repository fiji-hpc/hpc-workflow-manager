package cz.it4i.fiji.haas.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ShellRoutines {
	
	public static final Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.ui.ShellRoutines.class);
	
	public static void openDirectoryInBrowser(Path directory) {
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.open(directory.toFile());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
}
