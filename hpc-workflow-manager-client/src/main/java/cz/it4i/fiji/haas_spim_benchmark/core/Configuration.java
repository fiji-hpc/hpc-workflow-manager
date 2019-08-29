
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {
	
	private Configuration(){
		// Private constructor.
	}

	private static final Logger log = LoggerFactory.getLogger(
		Configuration.class);

	private static Properties properties;

	private static final int HAAS_UPDATE_TIMEOUT = 30000;
	private static final int HAAS_CLUSTER_NODE_TYPE = 7;
	private static final int HAAS_TEMPLATE_ID = 4;
	private static final String HAAS_PROJECT_ID = "DD-18-42";
	private static final int WALLTIME = 3600; // Walltime in seconds
	private static final String BDS_ADDRESS = "http://julius2.it4i.cz/";

	private static Properties getProperties() throws IOException {
		if (properties == null) {
			properties = new Properties();
			try (InputStream is = Configuration.class.getClassLoader()
				.getResourceAsStream("hpc_wm_configuration.propertie"))
			{
				properties.load(is);
			}
		}
		return properties;
	}

	public static int getHaasUpdateTimeout() {
		return getOrDefault("haas_update_timeout", HAAS_UPDATE_TIMEOUT);
	}

	public static int getHaasClusterNodeType() {
		return getOrDefault("haas_cluster_node_type", HAAS_CLUSTER_NODE_TYPE);
	}

	public static int getHaasTemplateID() {
		return getOrDefault("haas_template_ID", HAAS_TEMPLATE_ID);
	}

	public static int getWalltime() {
		return getOrDefault("walltime", WALLTIME);
	}

	public static String getHaasProjectID() {
		return getOrDefault("haas_project_ID", HAAS_PROJECT_ID);
	}

	public static String getBDSAddress() {
		return getOrDefault("bds_address", BDS_ADDRESS);
	}

	private static String getOrDefault(String name, String def) {
		try {
			return Optional.ofNullable(getProperties().getProperty(name)).orElse(def);
		}
		catch (IOException e) {
			log.error(e.getMessage(), e);
			return def;
		}
	}

	private static int getOrDefault(String name, int def) {
		try {
			return Optional.ofNullable(getProperties().getProperty(name)).map(
				val -> Integer.parseInt(val)).orElse(def);
		}
		catch (IOException e) {
			log.error(e.getMessage(), e);
			return def;
		}
	}
}
