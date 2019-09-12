
package cz.it4i.fiji.hpc_workflow.ui;

import java.awt.Window;

import org.scijava.prefs.PrefService;

import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.fiji.hpc_workflow.commands.HPCWorkflowParametersImpl;
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
		LastFormLoader<HPCWorkflowParametersImpl> storeLastForm =
			new LastFormLoader<>(prefService, "loginSettingsForm", this.getClass());
		HPCWorkflowParametersImpl oldLoginSettings = storeLastForm.loadLastForm();

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

		HPCWorkflowParametersImpl newSettings = this.controller.getParameters();

		// Save the new settings:
		storeLastForm.storeLastForm(newSettings);
	}

	public HPCWorkflowParametersImpl getParameters() {
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
