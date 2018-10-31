package cz.it4i.fiji.haas_java_client;

import java.util.Arrays;
import java.util.stream.LongStream;

import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDeleteJobs {
	
	private final static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_java_client.TestDeleteJobs.class);
	
	public static void main(String[] args) {
		
		Iterable<Long> iter = null;
		if (args[0].contains(",")) {
			iter =() -> Arrays.asList(args[0].split(",")).stream().map(Long::parseLong).iterator();
		} else {
			long first = Integer.parseInt(args[0]);
			long last = args.length > 1 ? Integer.parseInt(args[1]) : first;
			iter =() -> LongStream.range(first, last + 1).iterator();
		}
		HaaSClient client = new HaaSClient(SettingsProvider.getSettings( "OPEN-12-20", TestingConstants.CONFIGURATION_FILE_NAME));
		for ( long i : iter ) {
			try {
				JobInfo ji = client.obtainJobInfo(i);
				
				log.info("Delete job: " + i + " with state: " + ji.getState());
				client.deleteJob(i);
			}
			catch (WebServiceException e) {
				log.info("job = " + i + " was already deleted");
			}
		}
	}
}
