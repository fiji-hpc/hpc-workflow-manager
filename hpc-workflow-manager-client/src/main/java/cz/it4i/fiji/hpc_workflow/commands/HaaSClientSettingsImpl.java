
package cz.it4i.fiji.hpc_workflow.commands;

import java.nio.file.Path;
import java.nio.file.Paths;

import cz.it4i.fiji.haas_java_client.HaaSClientSettings;
import cz.it4i.fiji.hpc_workflow.core.Configuration;
import cz.it4i.fiji.hpc_workflow.paradigm_manager.SettingsWithWorkingDirectory;

public class HaaSClientSettingsImpl implements HaaSClientSettings,
	SettingsWithWorkingDirectory
{

	private static final long serialVersionUID = 1L;

	private final String userName;

	private final String password;

	private final String phone;

	private final String email;

	private final String workingDirectoryString;

	private transient Path workingDirectory;

	public HaaSClientSettingsImpl(String userName, String password,
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
	public String getUserName() {
		return userName;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getPhone() {
		return phone;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public String getProjectId() {
		return Configuration.getHaasProjectID();
	}

	@Override
	public Path getWorkingDirectory() {
		if (workingDirectory == null && workingDirectoryString != null) {
			workingDirectory = Paths.get(workingDirectoryString);
		}
		return workingDirectory;
	}

}
