package cz.it4i.fiji.haas;

import cz.it4i.fiji.haas.ui.FXFrameNative;
import cz.it4i.fiji.hpc_workflow.ui.JobDetailControl;

public class JobDetailControlRun {
	public static void main(String[] args) {
		new javafx.embed.swing.JFXPanel();
		class Window extends FXFrameNative<JobDetailControl> {

			public Window() {
				super(() -> new JobDetailControl(null));

			}

		}

		Window w;
		w = new Window();
		w.setVisible(true);

	}
}
