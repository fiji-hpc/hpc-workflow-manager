package cz.it4i.fiji.haas_java_client;

import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCommunicationWithNodes {

	public static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_java_client.TestCommunicationWithNodes.class);

	private static String[] predefined  = new String[2];
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException, InterruptedException {
		predefined[0] = "POST /modules/'command:net.imagej.ops.math.PrimitiveMath$IntegerAdd'?process=false HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Host: localhost:8080\r\n" +
                "Content-Length: 13\r\n" +
                "\r\n" +
                "{\"a\":1,\"b\":3}";
		
		predefined[1] = //
				"GET /modules HTTP/1.1\r\n" + // 
				"Host: localhost:8080\r\n" + //
				"User-Agent: curl/7.47.0\r\n" + //
				"Accept: */*\r\n" + //
				"\r\n";
		
		HaaSClientSettings settings = SettingsProvider.getSettings("OPEN-12-20",
				TestingConstants.CONFIGURATION_FILE_NAME);
		HaaSClient client = new HaaSClient(settings);
		long id = 376;//client.createJob("New job", Collections.emptyList());
		String sessionID = client.getSessionID();
		log.info(id + " - " + client.obtainJobInfo(id).getState() + " - " + sessionID);
		if(client.obtainJobInfo(id).getState() != JobState.Running && client.obtainJobInfo(id).getState() != JobState.Queued) {
			client.submitJob(id);
		}
		
		while (client.obtainJobInfo(id).getState() == JobState.Queued) {
			log.info("" + client.obtainJobInfo(id).getState());
			Thread.sleep(5000);
		}
		String ip;
		log.info("adresess " + (ip = client.obtainJobInfo(id).getNodesIPs().get(0)));
		try(TunnelToNode tunnel = client.openTunnel( id, ip, 8080, 8080)) {
			log.info(tunnel.getLocalHost() + ":" + tunnel.getLocalPort());
			System.out.println("Press enter");
			new Scanner(System.in).nextLine();
		}
	}

	

}
