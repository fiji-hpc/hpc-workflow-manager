package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Frame;

import cz.it4i.fiji.haas.ui.FXFrame;

public class BenchmarkSPIMWindow extends FXFrame<BenchmarkSPIMController> {

	private static final long serialVersionUID = 1L;

	public BenchmarkSPIMWindow(Frame applicationFrame) {
		super(applicationFrame, "/cz/it4i/fiji/haas_spim_benchmark/ui/BenchmarkSPIM.fxml");
		init(x->{});
	}

}
