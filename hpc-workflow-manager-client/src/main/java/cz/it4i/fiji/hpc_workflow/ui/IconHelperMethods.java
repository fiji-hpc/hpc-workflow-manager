
package cz.it4i.fiji.hpc_workflow.ui;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class IconHelperMethods {

	private IconHelperMethods() {
		// This constructor is empty to hide the implicit public one.
	}

	public static Image convertIkonToImage(Ikon icon) {
		try {
			FontIcon node = new FontIcon(icon);
			node.setIconSize(200);
			final SnapshotParameters snapPara = new SnapshotParameters();
			snapPara.setFill(Color.TRANSPARENT);
			// Do not remove the scene line:
			// https://stackoverflow.com/questions/53198858/how-to-use-a-fontawesomeiconview-object-as-a-stages-icon
			new Scene(new StackPane(node)); // Do not remove.
			Image myImage = node.snapshot(snapPara, null);
			return SwingFXUtils.toFXImage(SwingFXUtils.fromFXImage(myImage, null),
				null);
		}
		catch (Exception e) {
			JavaFXRoutines.runOnFxThread(() -> SimpleDialog.showException(
				"Window icon could not be loaded.",
				"The window icon could not be loaded.", e));
			return null;
		}

	}
}
