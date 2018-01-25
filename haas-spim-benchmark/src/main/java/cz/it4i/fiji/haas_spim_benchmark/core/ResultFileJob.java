package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.HashMap;
import java.util.Map;

public class ResultFileJob {
	String id;
	Map<String, String> values = new HashMap<>();

	public ResultFileJob(String id) {
		this.id = id;
	}

	public String getValue(String key) {
		return values.get(key);
	}

	public void setValue(String key, String value) {
		values.put(key, value);
	}
}