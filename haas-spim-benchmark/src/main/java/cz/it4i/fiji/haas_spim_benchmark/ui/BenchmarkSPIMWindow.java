package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.io.IOException;

import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkSPIMParameters;


public class BenchmarkSPIMWindow extends FXFrame<BenchmarkSPIMController>{

	private static final long serialVersionUID = 1L;
	

	
	public BenchmarkSPIMWindow(Window parentWindow, BenchmarkSPIMParameters params) throws IOException {
		super(parentWindow,()->{
			try {
				return new BenchmarkSPIMController(new BenchmarkJobManager(params));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		
	}
	
}
