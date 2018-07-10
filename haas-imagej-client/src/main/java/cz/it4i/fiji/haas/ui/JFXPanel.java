package cz.it4i.fiji.haas.ui;

import java.awt.Dimension;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * JFXPanel makes the link between Swing (IJ) and JavaFX plugin.
 */

public class JFXPanel<T extends Parent> extends javafx.embed.swing.JFXPanel {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.ui.JFXPanel.class);

	private final T control;

	public JFXPanel(Supplier<T> fxSupplier) {
		Platform.setImplicitExit(false);
		control = fxSupplier.get();
		// The call to runLater() avoid a mix between JavaFX thread and Swing thread.
		JavaFXRoutines.runOnFxThread(() -> initFX());
	}

	private void initFX() {
		// Init the root layout
		// Show the scene containing the root layout.
		Scene scene = new Scene(control);
		this.setScene(scene);
		this.setVisible(true);

		// Resize the JFrame to the JavaFX scene
		Dimension dim = new Dimension((int) scene.getWidth(), (int) scene.getHeight());
		this.setMinimumSize(dim);
		this.setMaximumSize(dim);
		this.setPreferredSize(dim);
	}
	
	public T getControl() {
		return control;
	}

}