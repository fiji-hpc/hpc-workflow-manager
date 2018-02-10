package cz.it4i.fiji.haas.ui;

import java.awt.Window;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public abstract class FXFrameNative<T extends Parent&CloseableControl> {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.ui.FXFrameNative.class);
	private JFXPanel<T> fxPanel;
	private Stage stage;

	public FXFrameNative(Supplier<T> fxSupplier) {
		this(null,fxSupplier);
	}

	public FXFrameNative(Window applicationFrame, Supplier<T> fxSupplier) {
		new javafx.embed.swing.JFXPanel();
		CloseableControl.runOnFxThread(() -> {
			stage = new Stage();
			stage.setTitle("My New Stage Title");
			stage.setScene(new Scene(fxSupplier.get(), 450, 450));
			stage.show();
		});
	}
	
	public void setVisible(boolean b) {
		CloseableControl.runOnFxThread(() -> {
			if(b) {
				stage.show();
			} else {
				stage.hide();
			}
		});
	}	

	


	public JFXPanel<T> getFxPanel() {
		return fxPanel;
	}
}
