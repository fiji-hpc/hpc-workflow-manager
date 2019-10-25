
package cz.it4i.fiji.hpc_workflow.commands;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowParameters;

public class HPCWorkflowParametersImpl implements HPCWorkflowParameters,
	Serializable
{

	private static final long serialVersionUID = 1L;

	private final String userName;

	private final String password;

	private final String phone;

	private final String email;

	private final String workingDirectoryString;

	private transient Path workingDirectory;

	public HPCWorkflowParametersImpl(String userName, String password,
		String phone, String email, Path workingDirectory)
	{
		this.userName = userName;
		this.password = password;
		this.phone = phone;
		this.email = email;
		this.workingDirectory = workingDirectory;
		this.workingDirectoryString = workingDirectory.toString();
	}

	@Override
	public String username() {
		return userName;
	}

	@Override
	public String password() {
		return password;
	}

	@Override
	public String phone() {
		return phone;
	}

	@Override
	public String email() {
		return email;
	}

	@Override
	public Path workingDirectory() {
		if (workingDirectory == null && workingDirectoryString != null) {
			workingDirectory = Paths.get(workingDirectoryString);
		}
		return workingDirectory;
	}

	public String workingDirectoryString() {
		return this.workingDirectoryString;
	}
}
