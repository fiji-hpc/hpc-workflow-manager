package cz.it4i.fiji.haas_snakemake_spim;

import cz.it4i.fiji.haas_java_client.Configuration;

public class Constants extends Configuration {

	public Constants() {
		super("configuration.properties");
	}

	public String getProjectId() {
		return getValue("PROJECT_ID");
	}

	public String getPassword() {
		return getValue("PASSWORD");
	}

	public String getUserName() {
		return getValue("USER_NAME");
	}

	public String getPhone() {
		return getValue("PHONE");
	}

	public String getEmail() {
		return getValue("EMAIL");
	}

}
