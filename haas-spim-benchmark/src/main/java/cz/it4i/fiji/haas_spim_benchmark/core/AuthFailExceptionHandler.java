
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.awt.Window;

import org.scijava.ui.DialogPrompt.MessageType;

import cz.it4i.fiji.haas_java_client.HaaSClientException;
import cz.it4i.fiji.scpclient.AuthFailException;

public class AuthFailExceptionHandler extends BaseExceptionHandler {

	public AuthFailExceptionHandler() {
		this(null);
	}

	public AuthFailExceptionHandler(final Window rootWindow) {
		super(rootWindow, (T, exc) -> (exc instanceof HaaSClientException && exc
			.getCause() instanceof AuthFailException), null,
			"Connection to HPC failed try again or contact software support.",
			MessageType.ERROR_MESSAGE);
	}
}
