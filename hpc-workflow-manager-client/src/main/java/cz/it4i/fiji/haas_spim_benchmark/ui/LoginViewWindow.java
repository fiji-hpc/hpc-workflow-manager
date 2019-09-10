
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;

import org.scijava.prefs.PrefService;

import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.haas_spim_benchmark.commands.BenchmarkSPIMParametersImpl;
import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginViewWindow extends FXFrame<LoginViewController> {

	private PrefService prefService;

	private LoginViewController controller;

	private static final long serialVersionUID = 1L;

	public LoginViewWindow(Window parentWindow) {
		super(parentWindow, LoginViewController::new);
		setTitle(Constants.SUBMENU_ITEM_NAME);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	public void openWindow() {
		// Get the previously entered login settings if any:
		LastFormLoader<BenchmarkSPIMParametersImpl> storeLastForm =
			new LastFormLoader<>(prefService, "loginSettingsForm", this.getClass());
		BenchmarkSPIMParametersImpl oldLoginSettings = storeLastForm.loadLastForm();

		// Open the login window:
		this.controller = new LoginViewController();
		final Scene formScene = new Scene(this.controller);
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setResizable(false);
		stage.setTitle("Local ImageJ Server Settings");
		stage.setScene(formScene);

		this.controller.setInitialFormValues(oldLoginSettings);

		stage.showAndWait();

		BenchmarkSPIMParametersImpl newSettings = this.controller.getParameters();

		// Save the new settings:
		storeLastForm.storeLastForm(newSettings);
	}

	public BenchmarkSPIMParametersImpl getParameters() {
		return this.controller.getParameters();
	}

	public void startJobDetailIfPossible() {
		this.controller.startJobDetailIfPossible();
	}

	public void initialize(PrefService newPrefService) {
		if (this.prefService == null) {
			this.prefService = newPrefService;
		}
	}
}
