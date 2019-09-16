
package cz.it4i.fiji.hpc_workflow.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import cz.it4i.fiji.commons.UncaughtExceptionHandlerDecorator;
import cz.it4i.fiji.hpc_workflow.commands.FileLock;
import cz.it4i.fiji.hpc_workflow.core.AuthFailExceptionHandler;
import cz.it4i.fiji.hpc_workflow.core.AuthenticationExceptionHandler;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowParameters;
import cz.it4i.fiji.hpc_workflow.core.NotConnectedExceptionHandler;
import cz.it4i.fiji.hpc_workflow.core.WindowCloseableAdapter;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
import cz.it4i.fiji.hpc_workflow.commands.HPCWorkflowParametersImpl;
import groovy.util.logging.Slf4j;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

@Slf4j
public class LoginViewController extends AnchorPane
{

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

	private static final String LOCK_FILE_NAME = ".lock";

	private static final String ERROR_HEADER = "Error";
	
	private FileLock fl;

	public LoginViewController() {
		JavaFXRoutines.initRootAndController("LoginView.fxml", this);
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

	private HPCWorkflowParameters parameters;

	@FXML
	private void okAction() {
		parameters = null;

		if (parametersAreFilledInAndCorrect()) {
			// Save parameters:
			this.parameters = getParameters();

			// Close the modal window:
			Stage stage = (Stage) okButton.getScene().getWindow();
			stage.close();
		}
	}

	public void startJobDetailIfPossible() {
		if (this.parameters != null) {
			final UncaughtExceptionHandlerDecorator uehd =
				UncaughtExceptionHandlerDecorator.setDefaultHandler();
			final WindowCloseableAdapter wca = new WindowCloseableAdapter();
			uehd.registerHandler(new AuthenticationExceptionHandler(wca));
			uehd.registerHandler(new NotConnectedExceptionHandler(wca));
			uehd.registerHandler(new AuthFailExceptionHandler());
			uehd.activate();

			new HPCWorkflowWindow(parameters, fl, uehd);
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

		File workingDirectory = new File(this.workingDirectoryTextField.getText());
		if (!workingDirectory.exists() || !workingDirectory.isDirectory()) {
			SimpleDialog.showError(ERROR_HEADER,
				"The working directory selected does not exist!");
			return false;
		}

		if (workingDirectoryIsUsedBySomeoneElse(workingDirectory)) {
			SimpleDialog.showError(ERROR_HEADER,
				"Working directory is already used by someone else.");
			return false;
		}
		return true;
	}

	private boolean workingDirectoryIsUsedBySomeoneElse(File workingDirectory) {
		try {
			final Path workingDirPath = Paths.get(workingDirectory.getPath());
			fl = new FileLock(workingDirPath.resolve(LOCK_FILE_NAME));
			if (!fl.tryLock()) {
				return true;
			}
		}
		catch (final IOException e) {
			SimpleDialog.showException(ERROR_HEADER,
				"Problem encountered while attempting to read file.", e);
		}
		return false;
	}

	public HPCWorkflowParametersImpl getParameters() {
		String userName = this.userNameTextField.getText();
		String password = this.passwordPasswordField.getText();
		String email = this.emailTextFiled.getText();
		Path workingDirPath = new File(this.workingDirectoryTextField.getText())
			.toPath();

		return new HPCWorkflowParametersImpl(userName, password, Constants.PHONE,
			email, workingDirPath);
	}

	public void setInitialFormValues(
		HPCWorkflowParametersImpl oldLoginSettings)
	{
		if (oldLoginSettings != null) {
			this.userNameTextField.setText(oldLoginSettings.username());
			this.passwordPasswordField.setText(oldLoginSettings.password());
			this.emailTextFiled.setText(oldLoginSettings.email());
			this.workingDirectoryTextField.setText(oldLoginSettings
				.workingDirectoryString());
		}

	}
}
