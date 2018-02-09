package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Frame;
import java.io.IOException;

import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkSPIMParameters;


public class BenchmarkSPIMWindow extends FXFrame<BenchmarkSPIMController>{

	private static final long serialVersionUID = 1L;
	

	
	public BenchmarkSPIMWindow(Frame applicationFrame, BenchmarkSPIMParameters params) throws IOException {
		super(applicationFrame,()->{
			try {
				return new BenchmarkSPIMController(new BenchmarkJobManager(params));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		
	}
	
}
