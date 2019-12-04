
package cz.it4i.fiji.hpc_adapter.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;

public interface ShellRoutines {

	public static void openDirectoryInBrowser(Path directory)
		throws UnsupportedOperationException, IOException
	{

		if (!Desktop.isDesktopSupported()) {
			throw new UnsupportedOperationException(
				"Desktop.getDesktop() is not supported on the current platform.");
		}

		Desktop desktop = Desktop.getDesktop();

		if (!desktop.isSupported(Desktop.Action.OPEN)) {
			throw new UnsupportedOperationException(
				"Desktop.open() is not supported on the current platform.");
		}

		desktop.open(directory.toFile());
	}
}
