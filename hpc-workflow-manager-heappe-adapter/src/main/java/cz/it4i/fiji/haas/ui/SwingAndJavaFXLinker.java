
package cz.it4i.fiji.haas.ui;

import java.awt.Dimension;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * JFXPanel makes the link between Swing (IJ) and JavaFX plugin.
 */

public class SwingAndJavaFXLinker<T extends Parent> extends javafx.embed.swing.JFXPanel {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas.ui.SwingAndJavaFXLinker.class);

	private final T control;

	public SwingAndJavaFXLinker(Supplier<T> fxSupplier) {
		Platform.setImplicitExit(false);
		control = fxSupplier.get();
		// The call to runLater() avoid a mix between JavaFX thread and Swing
		// thread.
		try {
			JavaFXRoutines.runOnFxThread(() -> initFX()).get();
		}
		catch (InterruptedException | ExecutionException exc) {
			log.error(exc.getMessage(), exc);
		}
	}

	private void initFX() {
		// Init the root layout
		// Show the scene containing the root layout.
		Scene scene = new Scene(control);
		this.setScene(scene);
		this.setVisible(true);

		// Resize the JFrame to the JavaFX scene
		Dimension dim = new Dimension((int) scene.getWidth(), (int) scene
			.getHeight());
		this.setMinimumSize(dim);
		this.setMaximumSize(dim);
		this.setPreferredSize(dim);
	}

	public T getControl() {
		return control;
	}

}
