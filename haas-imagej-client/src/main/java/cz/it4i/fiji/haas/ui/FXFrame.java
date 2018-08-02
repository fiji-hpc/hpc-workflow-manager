
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

public abstract class FXFrame<T extends Parent & CloseableControl> extends
	JDialog
{

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas.ui.FXFrame.class);
	private static final long serialVersionUID = 1L;
	private final JFXPanel<T> fxPanel;
	
	private boolean controlClosed;

	public FXFrame(Supplier<T> fxSupplier) {
		this(null, fxSupplier);
	}

	public FXFrame(Window parent, Supplier<T> fxSupplier) {
		super(parent, ModalityType.MODELESS);
		fxPanel = new JFXPanel<>(fxSupplier);
		init();
		if (fxPanel.getControl() instanceof InitiableControl) {
			InitiableControl control = (InitiableControl) fxPanel.getControl();
			control.init(this);
		}
	}

	public JFXPanel<T> getFxPanel() {
		return fxPanel;
	}

	public void executeAdjustment(Runnable command) {
		JavaFXRoutines.runOnFxThread(() -> {
			command.run();
		});
	}
	

	private void init() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeControlIfNotClose();
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				closeControlIfNotClose();
			}

			private void closeControlIfNotClose() {
				synchronized (FXFrame.this) {
					if(!controlClosed) {
						getFxPanel().getControl().close();
						controlClosed = true;
					}
				}
			}
		});
		
		if (fxPanel.getControl() instanceof ResizeableControl) {
			ResizeableControl resizable = (ResizeableControl) fxPanel.getControl();
			addComponentListener(new ComponentAdapter() {

				@Override
				public void componentResized(ComponentEvent e) {
					resizable.setSize(e.getComponent().getSize().getWidth(), e
						.getComponent().getSize().getHeight());
				}
			});
		}
		this.setLayout(new BorderLayout());
		this.add(fxPanel, BorderLayout.CENTER);
		this.pack();
		SwingRoutines.centerOnScreen(this);
	}
}
