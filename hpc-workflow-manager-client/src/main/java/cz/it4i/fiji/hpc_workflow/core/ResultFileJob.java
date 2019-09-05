package cz.it4i.fiji.hpc_workflow.core;

import java.util.HashMap;
import java.util.Map;

public class ResultFileJob {
	
	private final Map<String, String> values = new HashMap<>();

	public String getValue(String key) {
		return values.get(key);
	}

	public void setValue(String key, String value) {
		values.put(key, value);
	}
}