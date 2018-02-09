package cz.it4i.fiji.haas.ui;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Supplier;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.Parent;


public abstract class FXFrame<T extends Parent&CloseableControl> extends JDialog {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.ui.FXFrame.class);
	private static final long serialVersionUID = 1L;
	private JFXPanel<T> fxPanel;

	public FXFrame(Supplier<T> fxSupplier) {
		this(null,fxSupplier);
	}

	public FXFrame(Window applicationFrame, Supplier<T> fxSupplier) {
		super(applicationFrame, ModalityType.MODELESS);
		fxPanel = new JFXPanel<>(fxSupplier);
		if (fxPanel.getControl() instanceof InitiableControl) {
			InitiableControl control = (InitiableControl) fxPanel.getControl();
			control.init(this);
		}
		init();
	}

		/**
	 * Create the JFXPanel that make the link between Swing (IJ) and JavaFX plugin.
	 */
	private void init() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				getFxPanel().getControl().close();
			}
		});
		this.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(this.fxPanel);
		this.add(scrollPane, BorderLayout.CENTER);
		CloseableControl.runOnFxThread(() -> this.pack());
		
	}


	public JFXPanel<T> getFxPanel() {
		return fxPanel;
	}
}
