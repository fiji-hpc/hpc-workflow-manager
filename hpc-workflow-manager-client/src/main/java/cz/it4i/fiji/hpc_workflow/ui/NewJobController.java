
package cz.it4i.fiji.hpc_workflow.ui;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class NewJobController extends BorderPane {

	public enum DataLocation {
			DEMONSTRATION_ON_SERVER, WORK_DIRECTORY, CUSTOM_DIRECTORY
	}

	public enum WorkflowType {
			SPIM_WORKFLOW(4), MACRO_WORKFLOW(8);

		private final int haasTemplateID;

		private WorkflowType(int workflowType) {
			this.haasTemplateID = workflowType;
		}

		public int getHaasTemplateID() {
			return this.haasTemplateID;
		}

		public static WorkflowType forLong(long id) {
			for (WorkflowType workflows : values()) {
				if (workflows.haasTemplateID == id) {
					return workflows;
				}
			}
			throw new IllegalArgumentException("Invalid WorkflowType id: " + id);
		}

	}

	private static final Runnable EMPTY_NOTIFIER = () -> {};

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(NewJobController.class);

	@FXML
	private Button createButton;

	@FXML
	private ToggleGroup inputDataLocationToggleGroup;

	@FXML
	private ToggleGroup outputDataLocationToggleGroup;

	@FXML
	private ToggleGroup workflowSelectorToggleGroup;

	@FXML
	private RadioButton ownInputRadioButton;

	@FXML
	private RadioButton demoInputDataRadioButton;

	@FXML
	private RadioButton ownOutputRadioButton;

	@FXML
	private RadioButton workflowSpimRadioButton;

	@FXML
	private TextField inputDirectoryTextField;

	@FXML
	private TextField outputDirectoryTextField;

	@FXML
	private Spinner<Integer> numberOfNodesSpinner;

	@FXML
	private Button selectInputButton;

	@FXML
	private Button selectOutputButton;

	private DataLocation inputDataLocation;

	private DataLocation outputDataLocation;

	private WorkflowType workflowType;

	private Stage ownerWindow;

	private Runnable createPressedNotifier;

	private String userScriptName;

	public NewJobController() {
		JavaFXRoutines.initRootAndController("NewJobView.fxml", this);
		getStylesheets().add(getClass().getResource("NewJobView.css")
			.toExternalForm());
		createButton.setOnMouseClicked(x -> createPressed());
		inputDataLocationToggleGroup.selectedToggleProperty().addListener((v, old,
			n) -> selected(n, ownInputRadioButton));
		outputDataLocationToggleGroup.selectedToggleProperty().addListener((v, o,
			n) -> selected(n, ownOutputRadioButton));
		workflowSpimRadioButton.selectedProperty().addListener((v, o,
			n) -> selectedSpimWorkflow(n));
		initSelectButton(inputDirectoryTextField, selectInputButton);
		initSelectButton(outputDirectoryTextField, selectOutputButton);

		// Spinner value factory:
		SpinnerValueFactory<Integer> valueFactory =
			new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE,
				1);
		numberOfNodesSpinner.setValueFactory(valueFactory);
	}

	public void close() {
		// There is nothing to close.
	}

	public void init(Stage parentStage) {
		ownerWindow = parentStage;
	}

	public Path getInputDirectory(Path workingDirectory) {
		return getDirectory(inputDataLocation, inputDirectoryTextField.getText(),
			workingDirectory);
	}

	public Path getOutputDirectory(Path workingDirectory) {
		return getDirectory(outputDataLocation, outputDirectoryTextField.getText(),
			workingDirectory);
	}

	public int getNumberOfNodes() {
		return numberOfNodesSpinner.getValue();
	}

	public WorkflowType getWorkflowType() {
		return workflowType;
	}

	public void setCreatePressedNotifier(Runnable createPressedNotifier) {
		if (createPressedNotifier != null) {
			this.createPressedNotifier = createPressedNotifier;
		}
		else {
			this.createPressedNotifier = EMPTY_NOTIFIER;
		}
	}

	private void initSelectButton(TextField textField, Button button) {
		button.setOnAction(x -> {
			Window parent = ownerWindow.getScene().getWindow();

			if (workflowSpimRadioButton.isSelected()) {
				setTextFieldForDirectory(textField, parent);
			}
			else {
				setTextFieldForFile(textField, parent);
			}
		});
	}

	private void setTextFieldForDirectory(TextField folderString, Window parent) {
		// Get the path from the text field an set it as initial path for the
		// file
		// chooser if it exists:
		Path p = Paths.get(folderString.getText());
		DirectoryChooser dch = new DirectoryChooser();
		if (p.toFile().exists()) {
			dch.setInitialDirectory(p.toAbsolutePath().toFile());
		}

		// Set the selected directory as the new content of the text field:
		File selectedDirectory = dch.showDialog(parent);
		if (selectedDirectory != null) {
			folderString.setText(selectedDirectory.toString());
		}
	}

	private void setTextFieldForFile(TextField folderString, Window parent) {
		// Get the path from the text field an set it as initial path for the
		// file
		// chooser if it exists:
		Path p = Paths.get(folderString.getText());
		FileChooser fch = new FileChooser();
		if (p.toFile().exists()) {
			fch.setInitialDirectory(p.toAbsolutePath().toFile().getParentFile());
		}

		// Restrict file choice to Fiji Macro scripts only:
		FileChooser.ExtensionFilter extensionFilter =
			new FileChooser.ExtensionFilter("Fiji Macro script file (*.ijm)",
				"*.ijm");
		fch.getExtensionFilters().add(extensionFilter);

		// Set the selected directory as the new content of the text field:
		File selectedFile = fch.showOpenDialog(parent);
		if (selectedFile != null && !selectedFile.isDirectory()) {
			folderString.setText(selectedFile.toString());
		}
	}

	private Path getDirectory(DataLocation dataLocation, String selectedDirectory,
		Path workingDirectory)
	{
		switch (dataLocation) {
			case DEMONSTRATION_ON_SERVER:
				return null;
			case WORK_DIRECTORY:
				return workingDirectory;
			case CUSTOM_DIRECTORY:
				return getPathWithoutFile(selectedDirectory);
			default:
				throw new UnsupportedOperationException("Not support " + dataLocation);
		}
	}

	private Path getPathWithoutFile(String selectedDirectory) {
		Path path = Paths.get(selectedDirectory).toAbsolutePath();
		File file = path.toFile();
		if (file.isDirectory()) {
			return path;
		}
		String scriptName = path.getFileName().toString();
		setUserScriptName(scriptName);
		return path.getParent();
	}
	
	private void setUserScriptName(String newFilename) {
		if(!workflowSpimRadioButton.isSelected()) {
			this.userScriptName = newFilename;
		}
	}

	private void createPressed() {
		obtainValues();
		if (checkDirectoryLocationIfNeeded()) {
			// Close stage
			Stage stage = (Stage) createButton.getScene().getWindow();
			stage.close();
			this.createPressedNotifier.run();
		}
	}

	private boolean checkDirectoryLocationIfNeeded() {
		return checkDataLocationValue(inputDataLocation, inputDirectoryTextField
			.getText(), "input") && checkDataLocationValue(outputDataLocation,
				outputDirectoryTextField.getText(), "output");
	}

	private boolean checkDataLocationValue(DataLocation dataLocation,
		String directory, String type)
	{
		Path directoryPath = Paths.get(directory);
		if (dataLocation == DataLocation.CUSTOM_DIRECTORY && (!directoryPath
			.toFile().exists() || directory.isEmpty()))
		{
			String message = !directory.isEmpty()
				? "Directory %s for %s does not exist."
				: "Directory for %2$s is not selected.";
			SimpleDialog.showWarning("Invalid input provided", String.format(message,
				directoryPath.toAbsolutePath(), type));
			return false;
		}
		return true;
	}

	private void obtainValues() {
		inputDataLocation = obtainDataLocation(inputDataLocationToggleGroup);
		outputDataLocation = obtainDataLocation(outputDataLocationToggleGroup);
		workflowType = obtainWorkflowType(workflowSelectorToggleGroup);
	}

	private WorkflowType obtainWorkflowType(ToggleGroup group) {
		int backawardOrderOfSelected = group.getToggles().size() - group
			.getToggles().indexOf(group.getSelectedToggle());
		return WorkflowType.values()[WorkflowType.values().length -
			backawardOrderOfSelected];
	}

	private DataLocation obtainDataLocation(ToggleGroup group) {
		int backawardOrderOfSelected = group.getToggles().size() - group
			.getToggles().indexOf(group.getSelectedToggle());
		return DataLocation.values()[DataLocation.values().length -
			backawardOrderOfSelected];
	}

	private void selected(Toggle n, Parent disableIfNotSelected) {
		disableIfNotSelected.getChildrenUnmodifiable().forEach(node -> node
			.setDisable(n != disableIfNotSelected));
	}

	private void selectedSpimWorkflow(Boolean spimWorkflowIsSelected) {
		if (spimWorkflowIsSelected) {
			numberOfNodesSpinner.getValueFactory().setValue(1);
			numberOfNodesSpinner.setDisable(true);
			demoInputDataRadioButton.setDisable(false);
		}
		else {
			numberOfNodesSpinner.setDisable(false);
			demoInputDataRadioButton.setDisable(true);
			if (demoInputDataRadioButton.isSelected()) {
				ownInputRadioButton.setSelected(true);
			}
		}

		// Always reset the input directory to prevent mixing choosing a directory
		// with a script file:
		inputDirectoryTextField.setText("");
	}

	public String getUserScriptName() {
		return this.userScriptName;
	}

}
