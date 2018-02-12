package cz.it4i.fiji.haas.ui;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Supplier;

import javax.swing.JDialog;

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

	public FXFrame(Window parent, Supplier<T> fxSupplier) {
		super(parent, ModalityType.MODELESS);
		fxPanel = new JFXPanel<>(fxSupplier);
		if (fxPanel.getControl() instanceof InitiableControl) {
			InitiableControl control = (InitiableControl) fxPanel.getControl();
			control.init(this);
		}
		init();
	}

	private void init() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				getFxPanel().getControl().close();
			}
		});
		if (fxPanel.getControl() instanceof ResizeableControl) {
			ResizeableControl resizable = (ResizeableControl) fxPanel.getControl();
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					resizable.setSize(e.getComponent().getSize().getWidth(), e.getComponent().getSize().getHeight());
				}
			});
		}
		this.setLayout(new BorderLayout());
		//JScrollPane scrollPane = new JScrollPane(this.fxPanel);
		this.add(fxPanel, BorderLayout.CENTER);
		CloseableControl.runOnFxThread(() -> this.pack());
		
	}


	public JFXPanel<T> getFxPanel() {
		return fxPanel;
	}
}
