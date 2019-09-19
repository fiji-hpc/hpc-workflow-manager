
package cz.it4i.fiji.hpc_workflow.core;

import cz.it4i.fiji.haas_java_client.NotConnectedException;
import javafx.scene.control.Alert.AlertType;

public class NotConnectedExceptionHandler extends BaseExceptionHandler {

	public NotConnectedExceptionHandler() {
		super((t, exc) -> exc instanceof NotConnectedException,
			"Connection to HPC infrastructure failed",
			"Check your access to the Internet or contact a HPC administrator",
			AlertType.ERROR);
	}
}
