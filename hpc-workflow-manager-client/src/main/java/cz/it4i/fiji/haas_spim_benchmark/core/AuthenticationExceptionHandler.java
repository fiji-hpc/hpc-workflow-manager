
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.io.Closeable;

import org.scijava.ui.DialogPrompt.MessageType;

import cz.it4i.fiji.haas_java_client.AuthenticationException;

public class AuthenticationExceptionHandler extends BaseExceptionHandler {

	public AuthenticationExceptionHandler() {
		this(null);
	}

	public AuthenticationExceptionHandler(final Closeable closeable) {
		super(closeable, (T, exc) -> exc instanceof AuthenticationException,
			"Authentication failed", "Invalid username or password provided",
			MessageType.WARNING_MESSAGE);
	}
}
