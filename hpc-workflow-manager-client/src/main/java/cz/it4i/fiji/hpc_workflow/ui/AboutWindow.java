
package cz.it4i.fiji.hpc_workflow.ui;

import org.kordamp.ikonli.materialdesign.MaterialDesign;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.swing_javafx_ui.IconHelperMethods;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AboutWindow {

	public AboutWindow() {
		Platform.setImplicitExit(false);
	}

	public void openWindow() {
		// Open the the window:
		AboutViewController controller = new AboutViewController();
		final Scene formScene = new Scene(controller);
		Stage stage = new Stage();
		stage.initOwner(null);
		stage.initModality(Modality.NONE);
		stage.setResizable(true);
		stage.setTitle("About " + Constants.MENU_ITEM_NAME);
		stage.setScene(formScene);
		Image myImage = IconHelperMethods.convertIkonToImage(
			MaterialDesign.MDI_HELP);
		stage.getIcons().add(myImage);
		stage.show();
	}
}
