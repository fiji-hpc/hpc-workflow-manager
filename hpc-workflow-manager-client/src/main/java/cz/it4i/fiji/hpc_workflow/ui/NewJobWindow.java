
package cz.it4i.fiji.hpc_workflow.ui;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.scijava.plugin.Plugin;

import cz.it4i.fiji.haas_java_client.JobSettings;
import cz.it4i.fiji.haas_java_client.JobSettingsBuilder;
import cz.it4i.fiji.hpc_workflow.core.Configuration;
import cz.it4i.fiji.hpc_workflow.core.Constants;
import cz.it4i.fiji.hpc_workflow.paradigm_manager.heappe.HEAppEClientJobSettings;
import cz.it4i.fiji.hpc_workflow.ui.NewJobController.WorkflowType;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

@Plugin(type = JavaFXJobSettingsProvider.class)
public class NewJobWindow implements
	JavaFXJobSettingsProvider<HEAppEClientJobSettings>
{

	@Override
	public Class<HEAppEClientJobSettings> getTypeOfJobSettings() {
		return HEAppEClientJobSettings.class;
	}

	@Override
	public void provideJobSettings(Window parent,
		Consumer<HEAppEClientJobSettings> consumer)
	{
		final NewJobController controller = new NewJobController();
		controller.setCreatePressedNotifier(() -> consumer.accept(constructSettings(
			controller)));
		final Scene formScene = new Scene(controller);
		Stage stage = new Stage();
		stage.initOwner(parent);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setResizable(false);
		stage.setTitle("Create job");
		stage.setScene(formScene);
		finalizeOnStageClose(controller, stage);
		controller.init(stage);
		stage.showAndWait();
	}


	private static HEAppEClientJobSettings constructSettings(
		NewJobController newJobController)
	{
		JobSettings jobSetttings = new JobSettingsBuilder().jobName(
			Constants.HAAS_JOB_NAME).clusterNodeType(Configuration
				.getHaasClusterNodeType()).templateId(newJobController.getWorkflowType()
					.getHaasTemplateID())
			.walltimeLimit(Configuration.getWalltime()).numberOfCoresPerNode(
				Constants.CORES_PER_NODE).numberOfNodes(newJobController
					.getNumberOfNodes())
			.build();
		return new PJobWitdDirectorySettingsAdapter(jobSetttings) {

			private static final long serialVersionUID = 5998838289289128870L;

			@Override
			public String getUserScriptName() {
				return newJobController.getUserScriptName();
			}

			@Override
			public UnaryOperator<Path> getOutputPath() {
				return newJobController::getOutputDirectory;
			}

			@Override
			public UnaryOperator<Path> getInputPath() {
				return newJobController::getInputDirectory;
			}

			@Override
			public WorkflowType getWorkflowType() {
				return newJobController.getWorkflowType();
			}
		};

	}

	private static void finalizeOnStageClose(NewJobController controller,
		Stage stage)
	{
		stage.setOnCloseRequest((WindowEvent we) -> controller.close());
	}

	@AllArgsConstructor
	private abstract static class PJobWitdDirectorySettingsAdapter implements
		HEAppEClientJobSettings
	{

		private static final long serialVersionUID = 7219177839749763140L;
		@Delegate(types = JobSettings.class)
		private final JobSettings jobSettings;

	}
}
