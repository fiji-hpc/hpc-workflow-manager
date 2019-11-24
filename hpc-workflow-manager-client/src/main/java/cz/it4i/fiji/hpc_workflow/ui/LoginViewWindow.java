
package cz.it4i.fiji.hpc_workflow.ui;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;

import cz.it4i.fiji.hpc_workflow.commands.HaaSClientSettingsImpl;
import cz.it4i.parallel.paradigm_managers.ParadigmProfileSettingsEditor;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginViewWindow {

	@Plugin(type = ParadigmProfileSettingsEditor.class, priority = Priority.HIGH)
	public static class Editor implements
		ParadigmProfileSettingsEditor<HaaSClientSettingsImpl>
	{

		@Parameter
		private Context context;

		@Override
		public Class<HaaSClientSettingsImpl> getTypeOfSettings() {
			return HaaSClientSettingsImpl.class;
		}

		@Override
		public HaaSClientSettingsImpl edit(HaaSClientSettingsImpl settings) {
			LoginViewWindow loginViewWindow = new LoginViewWindow();
			context.inject(loginViewWindow);
			loginViewWindow.openWindow(settings);
			return loginViewWindow.getParameters();
		}

	}

	@Parameter
	private PrefService prefService;

	private LoginViewController controller;

	public void openWindow(HaaSClientSettingsImpl params) {
		// Get the previously entered login settings if any:
		LastFormLoader<HaaSClientSettingsImpl> storeLastForm =
			new LastFormLoader<>(prefService, "loginSettingsForm", this.getClass());
		HaaSClientSettingsImpl oldLoginSettings = params != null ? params
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

		HaaSClientSettingsImpl newSettings = this.controller.getParameters();

		// Save the new settings:
		storeLastForm.storeLastForm(newSettings);
	}

	public HaaSClientSettingsImpl getParameters() {
		return this.controller.getParameters();
	}

}
