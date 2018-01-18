package cz.it4i.fiji.haas.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.JDialog;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class FXFrame<C extends FXFrame.Controller> extends JDialog {

	public interface Controller {
		void init(Window frame);
	}

	private static final long serialVersionUID = 1L;
	private JFXPanel fxPanel;
	private String fxmlFile;
	private Consumer<C> controlerInit;
	private C controller;

	public FXFrame(String fxmlFile) {
		this(null, fxmlFile);
	}

	public FXFrame(Frame applicationFrame, String string) {
		super(applicationFrame);
		fxmlFile = string;
	}

	/**
	 * Create the JFXPanel that make the link between Swing (IJ) and JavaFX plugin.
	 */
	protected void init(Consumer<C> controlerInit) {
		this.controlerInit = controlerInit;
		this.fxPanel = new JFXPanel();
		Platform.setImplicitExit(false);
		this.add(this.fxPanel);

		// The call to runLater() avoid a mix between JavaFX thread and Swing thread.
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initFX(fxPanel);
			}

		});

	}

	protected C getController() {
		return controller;
	}

	private void initFX(JFXPanel fxPanel) {
		// Init the root layout
		try {
			FXMLLoader loader = new FXMLLoader();
			URL res = FXFrame.class.getResource(fxmlFile);
			loader.setLocation(res);
			Parent rootLayout = (Parent) loader.load();

			// Get the controller and add an ImageJ context to it.
			controller = loader.<C>getController();
			controlerInit.accept(controller);
			controller.init(this);
			// Show the scene containing the root layout.
			Scene scene = new Scene(rootLayout);
			this.fxPanel.setScene(scene);
			this.fxPanel.setVisible(true);

			// Resize the JFrame to the JavaFX scene
			Dimension dim = new Dimension((int) scene.getWidth(), (int) scene.getHeight());
			this.fxPanel.setMinimumSize(dim);
			this.fxPanel.setMaximumSize(dim);
			this.fxPanel.setPreferredSize(dim);
			// this.setSize((int) scene.getWidth(), (int) scene.getHeight());
			this.pack();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
