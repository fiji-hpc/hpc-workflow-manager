package cz.it4i.fiji.hpc_adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class PropertyHolder {
	private final Path storage;
	private Properties properties;

	public PropertyHolder(Path storage) {
		this.storage = storage;
	}

	public String getValue(String key) {
		Properties _properties = getProperties();
		return _properties.getProperty(key);
	}

	public void setValue(String key, String value) {
		Properties prop = getProperties();
		if(value != null) {
			prop.setProperty(key, value);
		} else {
			prop.remove(key);
		}
		try {
			storeProperties(prop);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Properties getProperties() {
		if (properties == null) {
			try {
				properties = loadPropertiesIfExists();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return properties;
	}

	private Properties loadPropertiesIfExists() throws IOException {
		Properties prop = new Properties();
		if (storage.toFile().exists()) {
			try (InputStream is = Files.newInputStream(storage)) {
				prop.load(is);
			}
		}
		return prop;
	}

	private void storeProperties(Properties prop) throws IOException {
		try (OutputStream ow = Files.newOutputStream(storage, StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.CREATE)) {
			prop.store(ow, null);
		}
	}
}
