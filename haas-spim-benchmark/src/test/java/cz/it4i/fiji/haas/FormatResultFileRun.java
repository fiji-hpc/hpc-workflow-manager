package cz.it4i.fiji.haas;

import java.nio.file.Paths;

import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager;

public class FormatResultFileRun {
	public static void main(String[] args) {
		BenchmarkJobManager.formatResultFile(Paths.get(args[0]));
	}
}
