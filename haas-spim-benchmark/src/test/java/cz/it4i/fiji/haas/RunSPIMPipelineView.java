package cz.it4i.fiji.haas;

import cz.it4i.fiji.haas.ui.FXFrameNative;

public class RunSPIMPipelineView {

	
	public static void main(String[] args) {
		class Window extends FXFrameNative<SPIMPipelineProgressViewController>{
	
			public Window() {
				super(()-> new SPIMPipelineProgressViewController());
				
			}
			
		}
		
		new Window().setVisible(true);
	}

}
