
package cz.it4i.fiji.hpc_workflow.ui;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import cz.it4i.fiji.hpc_workflow.commands.HPCWorkflowParametersImpl;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.fiji.hpc_workflow.paradigm_manager.WorkflowParadigmManager;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
import groovy.util.logging.Slf4j;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

@Slf4j
public class LoginViewController extends AnchorPane {

	@FXML
	Button okButton;

	@FXML
	Button browseButton;

	@FXML
	TextField userNameTextField;

	@FXML
	PasswordField passwordPasswordField;

	@FXML
	TextField emailTextFiled;

	@FXML
	TextField workingDirectoryTextField;

	private HPCWorkflowParametersImpl parameters;

	public LoginViewController() {
		JavaFXRoutines.initRootAndController("LoginView.fxml", this);
	}

	public void setInitialFormValues(HPCWorkflowParametersImpl oldLoginSettings) {
		if (oldLoginSettings != null) {
			this.userNameTextField.setText(oldLoginSettings.username());
			this.passwordPasswordField.setText(oldLoginSettings.password());
			this.emailTextFiled.setText(oldLoginSettings.email());
			this.workingDirectoryTextField.setText(oldLoginSettings
				.workingDirectoryString());
		}
	
	}

	public HPCWorkflowParametersImpl getParameters() {
		return parameters;
	}

	@FXML
	private void browseAction() {
		Stage stage = (Stage) browseButton.getScene().getWindow();
		File selectedDirectory = SimpleDialog.directoryChooser(stage,
			"Open Working Directory");
		if (selectedDirectory != null) {
			this.workingDirectoryTextField.setText(selectedDirectory
				.getAbsolutePath());
		}
	}

	@FXML
	private void okAction() {

		if (parametersAreFilledInAndCorrect()) {
			// Save parameters:
			this.parameters = constructParameters();

			// Close the modal window:
			Stage stage = (Stage) okButton.getScene().getWindow();
			stage.close();
		}

	}

	private boolean parametersAreFilledInAndCorrect() {
		// Check if parameters are filled in:
		if (this.userNameTextField.getText().isEmpty() || this.passwordPasswordField
			.getText().isEmpty() || this.emailTextFiled.getText().isEmpty() ||
			this.workingDirectoryTextField.getText().isEmpty())
		{
			SimpleDialog.showInformation("Missing fields",
				"Please fill in the whole form.");
			return false;
		}

		return WorkflowParadigmManager.checkWorkingDirectory(Paths.get(
			this.workingDirectoryTextField.getText()));

	}

	private HPCWorkflowParametersImpl constructParameters() {
		String userName = this.userNameTextField.getText();
		String password = this.passwordPasswordField.getText();
		String email = this.emailTextFiled.getText();
		Path workingDirPath = new File(this.workingDirectoryTextField.getText())
			.toPath();

		return new HPCWorkflowParametersImpl(userName, password, Constants.PHONE,
			email, workingDirPath);
	}
}
