package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.io.IOException;

import cz.it4i.fiji.haas.ui.FXFrame;

public class NewJobWindow extends FXFrame<NewJobController>{

	private static final long serialVersionUID = 1L;
	

	
	public NewJobWindow(Window parentWindow) throws IOException {
		super(parentWindow,()->{
			return new NewJobController();
			
		});
		setTitle("Create job");
	}
	
}