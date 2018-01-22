package cz.it4i.fiji.haas_spim_benchmark.core;

import java.nio.file.Path;

public interface BenchmarkSPIMParameters {
	Path workingDirectory();
	String username();
	String password();
	String phone();
	String email();
}
