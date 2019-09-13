package cz.it4i.fiji.haas;

import net.imagej.ImageJ;

import cz.it4i.fiji.hpc_workflow.commands.NewManageHPCWorkflow;

public class Main {
	public static void main(final String... args) throws Exception {
		// create the ImageJ application context with all available services
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		// invoke the plugin
		ij.command().run(NewManageHPCWorkflow.class, true);
	}
}
