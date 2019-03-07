package cz.it4i.fiji.haas;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.UploadingFileFromResource;
import cz.it4i.fiji.haas_java_client.UploadingFile;

public class TestUploadingData {
	private static Logger log = LoggerFactory.getLogger(TestUploadingData.class);
	public static void main(String[] args) throws IOException {
		UploadingFile uf = new UploadingFileFromResource("", "config.yaml");
		log.info("size: " + uf.getLength());
	}
}
