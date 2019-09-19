
package cz.it4i.fiji.hpc_workflow.core;

import cz.it4i.fiji.haas_java_client.HaaSClientException;
import cz.it4i.fiji.scpclient.AuthFailException;
import javafx.scene.control.Alert.AlertType;

public class AuthFailExceptionHandler extends BaseExceptionHandler {

	public AuthFailExceptionHandler() {
		super((t, exc) -> (exc instanceof HaaSClientException && exc
			.getCause() instanceof AuthFailException), null,
			"Connection to the HPC cluster failed. Try again or contact the software support.",
			AlertType.ERROR);
	}
}
