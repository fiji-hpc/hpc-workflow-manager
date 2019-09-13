
package cz.it4i.fiji.hpc_workflow.core;

import java.util.concurrent.Executor;

import cz.it4i.swing_javafx_ui.JavaFXRoutines;

public class FXFrameExecutorService implements Executor {

	@Override
	public void execute(Runnable command) {

		JavaFXRoutines.runOnFxThread(command::run);

	}

}
