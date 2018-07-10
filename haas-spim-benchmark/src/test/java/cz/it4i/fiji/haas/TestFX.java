package cz.it4i.fiji.haas;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.WindowConstants;

import cz.it4i.fiji.haas_spim_benchmark.ui.BenchmarkSPIMControl;

public class TestFX {
	public static void main(String[] args) {
		
		class Window extends FXFrame<BenchmarkSPIMControl>{

			public Window() {
				super(()->new BenchmarkSPIMControl(null));
			}
			
		}
		Window window;
		(window = new Window()).setVisible(true);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				System.exit(0);
			}
		});
	}
}
