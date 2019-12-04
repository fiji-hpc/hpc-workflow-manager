
package cz.it4i.fiji.hpc_workflow.core;

import cz.it4i.fiji.hpc_client.AuthenticationException;
import javafx.scene.control.Alert.AlertType;

public class AuthenticationExceptionHandler extends BaseExceptionHandler {

	public AuthenticationExceptionHandler() {
		super((t, exc) -> exc instanceof AuthenticationException,
			"Authentication failed", "Invalid username or password provided",
			AlertType.WARNING);
	}
}
