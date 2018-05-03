package cz.it4i.fiji.haas_spim_benchmark.commands;

import java.nio.file.Path;

import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkSPIMParameters;

class BenchmarkSPIMParametersImpl implements BenchmarkSPIMParameters{

	private String userName;
	
	private String password;
	
	private String phone;
	
	private String email;
	
	private Path workingDirectory;
	
	public BenchmarkSPIMParametersImpl(String userName, String password, String phone, String email, Path workingDirectory) {
		this.userName = userName;
		this.password = password;
		this.phone = phone;
		this.email = email;
		this.workingDirectory = workingDirectory;
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
		return workingDirectory;
	}
}
