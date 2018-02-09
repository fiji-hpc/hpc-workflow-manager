package cz.it4i.fiji.haas.ui;

import java.awt.BorderLayout;
import java.awt.Window;
import java.util.function.Consumer;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

public class FXFrame<C extends JFXPanelWithController.Controller> extends JDialog {

	private static final long serialVersionUID = 1L;
	private JFXPanelWithController<C> fxPanel;
	private String fxmlFile;

	public FXFrame(String fxmlFile) {
		this(null, fxmlFile);
	}

	public FXFrame(Window applicationFrame, String string) {
		super(applicationFrame, ModalityType.MODELESS);
		fxmlFile = string;
	}

	/**
	 * Create the JFXPanel that make the link between Swing (IJ) and JavaFX plugin.
	 */
	protected void init(Consumer<C> controlerInit) {

		this.fxPanel = new JFXPanelWithController<C>(this, fxmlFile, controlerInit);

		this.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(this.fxPanel);

		this.add(scrollPane, BorderLayout.CENTER);

		JFXPanelWithController.runOnFxThread(() -> this.pack());
	}

	protected C getController() {
		return fxPanel.getController();
	}

}
