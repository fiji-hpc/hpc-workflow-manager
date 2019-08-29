package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;

import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkSPIMParameters;


public class BenchmarkSPIMWindow extends FXFrame<BenchmarkSPIMControl>{

	private static final long serialVersionUID = 1L;
		
	public BenchmarkSPIMWindow(Window parentWindow, BenchmarkSPIMParameters params) {
		super(parentWindow, () -> new BenchmarkSPIMControl(new BenchmarkJobManager(
			params)));
		
	}
}
