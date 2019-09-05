package cz.it4i.fiji.hpc_workflow.core;

import java.nio.file.Path;

public interface HPCWorkflowParameters {
	String username();
	String password();
	String phone();
	String email();
	Path workingDirectory();
}
