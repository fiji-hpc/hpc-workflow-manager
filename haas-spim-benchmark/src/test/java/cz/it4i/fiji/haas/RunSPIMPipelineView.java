package cz.it4i.fiji.haas;

public class RunSPIMPipelineView {

	
	public static void main(String[] args) {
		@SuppressWarnings("serial")
		class Window extends FXFrame<SPIMPipelineProgressViewController>{
	
			public Window() {
				super(()-> new SPIMPipelineProgressViewController());
				
			}
			
		}
		
		new Window().setVisible(true);
	}

}
