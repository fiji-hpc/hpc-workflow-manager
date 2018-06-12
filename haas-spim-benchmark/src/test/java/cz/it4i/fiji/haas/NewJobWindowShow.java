package cz.it4i.fiji.haas;

import java.io.IOException;

import cz.it4i.fiji.haas_spim_benchmark.ui.NewJobWindow;

public class NewJobWindowShow {
	public static void main(String[] args) throws IOException {
		new NewJobWindow(null).setVisible(true);
	}
}
