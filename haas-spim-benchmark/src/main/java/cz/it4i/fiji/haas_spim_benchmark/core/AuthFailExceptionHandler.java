
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.awt.Window;
import java.util.function.BiPredicate;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.DialogPrompt.MessageType;
import org.scijava.ui.UIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClientException;
import cz.it4i.fiji.scpclient.AuthFailException;

public class AuthFailExceptionHandler implements
	BiPredicate<Thread, Throwable>
{

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_spim_benchmark.core.AuthFailExceptionHandler.class);
	
	@Parameter
	private UIService uiService;
	
	final private Window rootWindow;

	public AuthFailExceptionHandler() {
		this(null);
	}
	
	public AuthFailExceptionHandler(final Window rootWindow) {
		this.rootWindow = rootWindow;
		uiService = new Context().getService(UIService.class);
	}

	@Override
	public boolean test(final Thread t, final Throwable exc) {
		if (exc instanceof HaaSClientException && exc
			.getCause() instanceof AuthFailException)
		{
			if(rootWindow != null) {
				rootWindow.dispose();
			}
			uiService.showDialog(
				"Connection to HPC failed try again or contact software support.",
				MessageType.ERROR_MESSAGE);
			log.info("Caught exception: " + exc.getMessage(), exc);
			return true;
		}
		return false;
	}

}
