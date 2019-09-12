package cz.it4i.fiji.haas;

import java.nio.file.Paths;

import cz.it4i.fiji.hpc_workflow.core.HPCWorkflowJobManager;

public class FormatResultFileRun {
	public static void main(String[] args) {
		HPCWorkflowJobManager.formatResultFile(Paths.get(args[0]));
	}
}
