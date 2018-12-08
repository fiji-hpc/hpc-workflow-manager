
package cz.it4i.fiji.haas_java_client;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConcurentAccessToHaaSFileTransfer {

	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_java_client.TestConcurentAccessToHaaSFileTransfer.class);

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		HaaSClient client = new HaaSClient(SettingsProvider.getSettings("DD-18-42",
			TestingConstants.CONFIGURATION_FILE_NAME));
		HaaSFileTransfer tr1 = client.startFileTransfer(250,
			HaaSClient.DUMMY_TRANSFER_FILE_PROGRESS);
		HaaSFileTransfer tr2 = client.startFileTransfer(249,
			HaaSClient.DUMMY_TRANSFER_FILE_PROGRESS);
		log.info("config.yaml - size:" + tr1.obtainSize(Arrays.asList(
			"config.yaml")));
		tr1.close();
		log.info("config.yaml - size:" + tr2.obtainSize(Arrays.asList(
			"config.yaml")));
		tr2.close();
	}

}
