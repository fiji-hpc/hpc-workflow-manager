
package cz.it4i.fiji.hpc_workflow.core;

import java.util.function.BiPredicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
import javafx.scene.control.Alert.AlertType;

public class BaseExceptionHandler implements BiPredicate<Thread, Throwable> {

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_workflow.core.BaseExceptionHandler.class);

	private final BiPredicate<Thread, Throwable> test;

	private final String title;

	private final String message;

	private final AlertType messageType;

	public BaseExceptionHandler(final BiPredicate<Thread, Throwable> test,
		final String title, final String message, final AlertType type)
	{
		this.test = test;
		this.title = title;
		this.message = message;
		this.messageType = type;
	}

	@Override
	public boolean test(final Thread t, final Throwable exc) {
		if (test.test(t, exc)) {
			if (messageType == AlertType.ERROR) {
				JavaFXRoutines.runOnFxThread(() -> SimpleDialog.showError(title,
					message));
			}
			else if (messageType == AlertType.WARNING) {
				JavaFXRoutines.runOnFxThread(() -> SimpleDialog.showWarning(title,
					message));
			}

			if (log.isDebugEnabled()) {
				log.debug("Caught exception: " + exc.getMessage(), exc);
			}
			return true;
		}
		return false;
	}
}
