
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.io.Closeable;

import org.scijava.ui.DialogPrompt.MessageType;

import cz.it4i.fiji.haas_java_client.HaaSClientException;
import cz.it4i.fiji.scpclient.AuthFailException;

public class AuthFailExceptionHandler extends BaseExceptionHandler {

	public AuthFailExceptionHandler() {
		this(null);
	}

	public AuthFailExceptionHandler(final Closeable closeable) {
		super(closeable, (t, exc) -> (exc instanceof HaaSClientException && exc
			.getCause() instanceof AuthFailException), null,
			"Connection to the HPC cluster failed. Try again or contact the software support.",
			MessageType.ERROR_MESSAGE);
	}
}
