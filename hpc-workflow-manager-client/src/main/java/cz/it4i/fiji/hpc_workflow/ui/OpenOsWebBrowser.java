
package cz.it4i.fiji.hpc_workflow.ui;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;

public class OpenOsWebBrowser {

	public static void openUrl(final String url) {
		final String myOS = System.getProperty("os.name").toLowerCase();
		try {
			if (myOS.contains("mac")) {
				Runtime.getRuntime().exec("open " + url);
			}
			else if (myOS.contains("nux") || myOS.contains("nix")) {
				Runtime.getRuntime().exec("xdg-open " + url);
			}
			else if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(new URI(url));
			}
			else {
				System.out.println("Please, open this URL yourself: " + url);
			}
		}
		catch (IOException | URISyntaxException ignored) {}
	}
}
