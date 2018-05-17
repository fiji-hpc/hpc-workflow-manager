package cz.it4i.fiji.haas;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class JFXPanel<T extends Parent> extends javafx.embed.swing.JFXPanel {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.ui.JFXPanel.class);

	private final T control;
	
	public JFXPanel(Supplier<T> fxSupplier) {
		Platform.setImplicitExit(false);
		// The call to runLater() avoid a mix between JavaFX thread and Swing thread.
		control = fxSupplier.get();
		initFX();
	}

	
	private void initFX() {
		// Init the root layout
		// Show the scene containing the root layout.
		Scene scene = new Scene(control);
		this.setScene(scene);
		this.setVisible(true);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub
				super.componentResized(e);
				//((SPIMPipelineProgressViewController)control).setPrefSize(e.getComponent().getWidth(), e.getComponent().getHeight());
				
			}
		});
		// Resize the JFrame to the JavaFX scene
//		Dimension dim = new Dimension((int) scene.getWidth(), (int) scene.getHeight());
//		this.setMinimumSize(dim);
//		this.setMaximumSize(dim);
//		this.setPreferredSize(dim);
		// this.setSize((int) scene.getWidth(), (int) scene.getHeight());
	};
	
	public T getControl() {
		return control;
	}

}