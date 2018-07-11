
package kozusznik.haas_experiment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.HaaSClientSettings;
import cz.it4i.fiji.haas_java_client.HaaSDataTransfer;
import cz.it4i.fiji.haas_java_client.SettingsProvider;

/**
 * Demonstrate failure reported in
 * https://vpsupport.vsb.cz:8002/redmine/issues/1115
 */
class TestConnectionCloseCase {

	public static final Logger log = LoggerFactory.getLogger(
		kozusznik.haas_experiment.TestConnectionCloseCase.class);
	private final static String MSG = //
		"GET /data/ HTTP/1.1\r\n" + //
			"Host: localhost:8080\r\n" + //
			"User-Agent: curl/7.47.0\r\n" + //
			"Accept: */*\r\n" + //
			"\r\n";
	private static HaaSClient client;
	private static long jobID;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		final HaaSClientSettings settings = SettingsProvider.getSettings(
			"OPEN-12-20", TestingConstants.CONFIGURATION_FILE_NAME);
		client = new HaaSClient(settings);
		jobID = Routines.startBDS(client);

	}

	@BeforeEach
	void setUp() throws Exception {}

	@Test
	void test$1Scenarion() {

		try (HaaSDataTransfer dataTransfer = client.startDataTransfer(jobID, 0,
			8081))
		{
			log.info("#1 scenario - #1 request");
			dataTransfer.write(MSG.getBytes());
			log.info("#1 scenario - response of #1 request:");
			logResponse(dataTransfer.read());
			dataTransfer.closeConnection();

			log.info("#1 scenario - #2 request");
			dataTransfer.write(MSG.getBytes());

			log.info("#1 scenario - response of #2 request:");
			byte[] response;
			logResponse(response = dataTransfer.read());
			assertNotNull(response);
			dataTransfer.closeConnection();
		}
		catch (final IOException e) {
			fail(e.getMessage(), e);
		}

	}

	@Test
	void test$2Scenario() throws Exception {
		log.info("#2 scenario");
		try (HaaSDataTransfer dataTransfer = client.startDataTransfer(jobID, 0,
			8081))
		{
			log.info("#2 scenario - #1 request");
			dataTransfer.write(MSG.getBytes());
			log.info("#2 scenario - response of #1 request:");
			logResponse(dataTransfer.read());
			dataTransfer.closeConnection();

			log.info("try to read before write:");
			// MAY BLOCK FOR READ
			final CompletableFuture<?> f = CompletableFuture.runAsync(
				() -> logResponse(dataTransfer.read()));
			Thread.sleep(3000);
			assertFalse(f.isDone());

			dataTransfer.closeConnection();
		}
	}

	private static void logResponse(final byte[] data) {
		if (data == null) {
			log.warn("Closed connection from middleware");
		}
		else {
			log.info(new String(data));
		}
	}

}
