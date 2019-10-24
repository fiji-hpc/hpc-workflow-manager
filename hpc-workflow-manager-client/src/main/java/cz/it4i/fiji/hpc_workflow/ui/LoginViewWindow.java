
package cz.it4i.fiji.hpc_workflow.ui;

import org.scijava.prefs.PrefService;

import cz.it4i.fiji.hpc_workflow.commands.HPCWorkflowParametersImpl;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginViewWindow {

	private PrefService prefService;

	private LoginViewController controller;

	public void openWindow(HPCWorkflowParametersImpl params) {
		// Get the previously entered login settings if any:
		LastFormLoader<HPCWorkflowParametersImpl> storeLastForm =
			new LastFormLoader<>(prefService, "loginSettingsForm", this.getClass());
		HPCWorkflowParametersImpl oldLoginSettings = params != null ? params
			: storeLastForm.loadLastForm();

		// Open the login window:
		this.controller = new LoginViewController();
		final Scene formScene = new Scene(this.controller);
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setResizable(false);
		stage.setTitle("Login");
		stage.setScene(formScene);

		this.controller.setInitialFormValues(oldLoginSettings);

		stage.showAndWait();

		HPCWorkflowParametersImpl newSettings = this.controller.getParameters();

		// Save the new settings:
		storeLastForm.storeLastForm(newSettings);
	}

	public HPCWorkflowParametersImpl getParameters() {
		return this.controller.getParameters();
	}



	public void initialize(PrefService newPrefService) {
		if (this.prefService == null) {
			this.prefService = newPrefService;
		}
	}
}
