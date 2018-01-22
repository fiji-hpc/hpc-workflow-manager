package cz.it4i.fiji.haas_java_client;

class Constants extends Configuration{

	
	public Constants() {
		super("configuration.properties");
	}

	public String getUserName() {
		return getValue("USER_NAME");
	}

	public String getPhone() {
		return getValue("PHONE");
	}

	public String getPassword() {
		return getValue("PASSWORD");
	}

	public String getEmail() {
		return getValue("EMAIL");
	}
	
	
	
}