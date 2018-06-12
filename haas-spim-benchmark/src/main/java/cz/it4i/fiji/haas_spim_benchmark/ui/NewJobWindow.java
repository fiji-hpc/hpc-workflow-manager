package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.nio.file.Path;

import cz.it4i.fiji.haas.ui.FXFrame;

public class NewJobWindow extends FXFrame<NewJobController>{

	private static final long serialVersionUID = 1L;
	

	
	public NewJobWindow(Window parentWindow) {
		super(parentWindow,()->{
			return new NewJobController();
			
		});
		setTitle("Create job");
	}
	
	public Path getInputDirectory(Path workingDirectory) {
		return getFxPanel().getControl().getInputDirectory(workingDirectory);
	}
	
	public Path getOutputDirectory(Path workingDirectory) {
		return getFxPanel().getControl().getOutputDirectory(workingDirectory);
	}


	public void setCreatePressedNotifier(Runnable runnable) {
		getFxPanel().getControl().setCreatePressedNotifier(runnable);
	}
}