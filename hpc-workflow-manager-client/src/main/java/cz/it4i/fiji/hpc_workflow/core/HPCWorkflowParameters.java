package cz.it4i.fiji.hpc_workflow.core;

import java.io.Serializable;
import java.nio.file.Path;

public interface HPCWorkflowParameters extends Serializable {
	String username();
	String password();
	String phone();
	String email();
	Path workingDirectory();
}
