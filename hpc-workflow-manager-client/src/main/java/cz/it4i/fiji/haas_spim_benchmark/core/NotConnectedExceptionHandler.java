
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.io.Closeable;

import org.scijava.ui.DialogPrompt.MessageType;

import cz.it4i.fiji.haas_java_client.NotConnectedException;

public class NotConnectedExceptionHandler extends BaseExceptionHandler {

	public NotConnectedExceptionHandler() {
		this(null);
	}

	public NotConnectedExceptionHandler(final Closeable closeable) {
		super(closeable, (T, exc) -> exc instanceof NotConnectedException,
			"Connection to HPC infrastructure failed",
			"Check your access to the Internet or contact a HPC administrator",
			MessageType.ERROR_MESSAGE);
	}
}
