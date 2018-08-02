
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.awt.Window;
import java.util.function.BiPredicate;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.DialogPrompt.MessageType;
import org.scijava.ui.UIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseExceptionHandler implements BiPredicate<Thread, Throwable> {

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_spim_benchmark.core.BaseExceptionHandler.class);

	@Parameter
	private final UIService uiService;

	private final Window rootWindow;

	private final BiPredicate<Thread, Throwable> test;

	private final String title;

	private final String message;

	private final MessageType messageType;

	public BaseExceptionHandler(final Window rootWindow,
		final BiPredicate<Thread, Throwable> test, final String title,
		final String message, final MessageType type)
	{
		this.rootWindow = rootWindow;
		uiService = new Context().getService(UIService.class);
		this.test = test;
		this.title = title;
		this.message = message;
		this.messageType = type;
	}

	@Override
	public boolean test(final Thread t, final Throwable exc) {
		if (test.test(t, exc)) {
			if (rootWindow != null) {
				rootWindow.dispose();
			}
			uiService.showDialog(message, title, messageType);
			log.info("Caught exception: " + exc.getMessage(), exc);
			return true;
		}
		return false;
	}
}
