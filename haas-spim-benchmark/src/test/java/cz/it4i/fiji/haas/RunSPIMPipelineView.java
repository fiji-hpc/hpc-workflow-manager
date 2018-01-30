package cz.it4i.fiji.haas;

import java.io.IOException;

import cz.it4i.fiji.haas_spim_benchmark.ui.SPIMPipelineProgressViewWindow;

public class RunSPIMPipelineView {

	public static void main(String[] args) throws IOException {
		new SPIMPipelineProgressViewWindow(null,null).setVisible(true);

	}

}
