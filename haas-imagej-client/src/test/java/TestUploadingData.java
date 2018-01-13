import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.UploadingFileFromResource;
import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;

public class TestUploadingData {
	private static Logger log = LoggerFactory.getLogger(TestUploadingData.class);
	public static void main(String[] args) {
		UploadingFile uf = new UploadingFileFromResource("", "config.yaml");
		log.info("size: " + uf.getLength());
	}
}
