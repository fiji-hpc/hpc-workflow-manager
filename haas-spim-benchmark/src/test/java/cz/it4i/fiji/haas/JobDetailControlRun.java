package cz.it4i.fiji.haas;

import cz.it4i.fiji.haas.ui.FXFrameNative;
import cz.it4i.fiji.haas_spim_benchmark.ui.JobDetailControl;

public class JobDetailControlRun {
	public static void main(String[] args) {
		new javafx.embed.swing.JFXPanel();
		class Window extends FXFrameNative<JobDetailControl> {

			public Window() {
				super(() -> new JobDetailControl());

			}

		}

		Window w;
		w = new Window();
		w.setVisible(true);

	}
}
