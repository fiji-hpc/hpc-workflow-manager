package cz.it4i.fiji.haas;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import cz.it4i.fiji.haas_spim_benchmark.ui.BenchmarkSPIMController;

public class TestFX {
	public static void main(String[] args) {
		@SuppressWarnings("serial")
		class Window extends FXFrame<BenchmarkSPIMController>{

			public Window() {
				super(()->new BenchmarkSPIMController(null));
				// TODO Auto-generated constructor stub
			}
			
		}
		Window window;
		(window = new Window()).setVisible(true);
		window.setDefaultCloseOperation(Window.DISPOSE_ON_CLOSE);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				super.windowClosed(e);
				System.exit(0);
			}
		});
	}
}
