package cz.it4i.fiji.haas;

import java.nio.file.Path;

import cz.it4i.fiji.haas_java_client.Configuration;
import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowParameters;

class TestBenchmarkSPIMParametersImpl extends Configuration implements HPCWorkflowParameters{

	private final Path workingDirectory;
	
	public TestBenchmarkSPIMParametersImpl(Path workingDirectory) {
		super("configuration.properties");
		this.workingDirectory = workingDirectory;
	}

	@Override
	public Path workingDirectory() {
		return workingDirectory;
	}

	@Override
	public String username() {
		return getValue("USER_NAME");
	}

	@Override
	public String password() {
		return getValue("PASSWORD");
	}

	@Override
	public String phone() {
		return getValue("PHONE");
	}

	@Override
	public String email() {
		return getValue("EMAIL");
	}
}
