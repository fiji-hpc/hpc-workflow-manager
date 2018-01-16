package cz.it4i.fiji.haas;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class TestYaml {

	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.TestYaml.class);
	
	public static void main(String[] args) throws IOException {
		try(InputStream is = args.getClass().getResourceAsStream("config.yaml")){
			Yaml yaml = new Yaml();
			
			Map map = yaml.load(is);
			log.info("common.hdf5_xml_filename: "  + ((Map)map.get("common")).get("hdf5_xml_filename"));
		}

	}

}
