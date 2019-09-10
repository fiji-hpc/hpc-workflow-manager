
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.WindowConstants;

import cz.it4i.fiji.commons.UncaughtExceptionHandlerDecorator;
import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas_spim_benchmark.commands.BenchmarkSPIMParametersImpl;
import cz.it4i.fiji.haas_spim_benchmark.commands.FileLock;
import cz.it4i.fiji.haas_spim_benchmark.core.AuthFailExceptionHandler;
import cz.it4i.fiji.haas_spim_benchmark.core.AuthenticationExceptionHandler;
import cz.it4i.fiji.haas_spim_benchmark.core.Constants;
import cz.it4i.fiji.haas_spim_benchmark.core.NotConnectedExceptionHandler;
import cz.it4i.fiji.haas_spim_benchmark.core.WindowCloseableAdapter;
import groovy.util.logging.Slf4j;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

@Slf4j
public class LoginViewController extends AnchorPane implements CloseableControl,
	InitiableControl
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

	private BenchmarkSPIMParametersImpl parameters;

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

			BenchmarkSPIMWindow dialog = new BenchmarkSPIMWindow(null, parameters);

			dialog.executeAdjustment(() -> {
				dialog.setTitle(Constants.SUBMENU_ITEM_NAME);
				dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				wca.setWindowAndShowIt(dialog);
			});
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
			final FileLock fl = new FileLock(workingDirPath.resolve(LOCK_FILE_NAME));
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

	@Override
	public void init(Window parameter) {
		// Nothing to do.
	}

	@Override
	public void close() {
		// Nothing to do.
	}

	public BenchmarkSPIMParametersImpl getParameters() {
		String userName = this.userNameTextField.getText();
		String password = this.passwordPasswordField.getText();
		String email = this.emailTextFiled.getText();
		Path workingDirPath = new File(this.workingDirectoryTextField.getText())
			.toPath();

		return new BenchmarkSPIMParametersImpl(userName, password, Constants.PHONE,
			email, workingDirPath);
	}

	public void setInitialFormValues(
		BenchmarkSPIMParametersImpl oldLoginSettings)
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
