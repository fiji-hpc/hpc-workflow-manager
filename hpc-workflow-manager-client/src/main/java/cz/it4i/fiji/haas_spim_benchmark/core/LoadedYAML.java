package cz.it4i.fiji.haas_spim_benchmark.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import org.yaml.snakeyaml.Yaml;

public class LoadedYAML {

	private Map<String, Map<String, String>> map;

	public LoadedYAML(InputStream openFile) throws IOException {
		try (InputStream is = openFile) {
			Yaml yaml = new Yaml();
			map = yaml.load(is);
			
		}
	}
	
	public String getCommonProperty(String name) {
		String result = Optional.ofNullable(map).map(m -> m.get("common")).map(m -> m.get(name))
				.orElse(null);
		if (result == null) {
			throw new IllegalArgumentException("hdf5_xml_filename not found");
		}
		if (result.charAt(0) == '"' || result.charAt(0) == '\'') {
			if (result.charAt(result.length() - 1) != result.charAt(0)) {
				throw new IllegalArgumentException(result);
			}
			result = result.substring(1, result.length() - 1);
		}

		return result;
	}
}
