package cz.it4i.fiji.haas_java_client;

public interface SettingsProvider {
	
	
	static HaaSClientSettings getSettings(String projectId, String configFileName) {
		Constants constants = new Constants(configFileName);
		return new HaaSClientSettings() {
			
			@Override
			public String getUserName() {
				return  constants.getUserName();
			}
			
			@Override
			public String getPhone() {
				return constants.getPhone();
			}
			
			@Override
			public String getPassword() {
				return constants.getPassword();
			}
			
			@Override
			public String getEmail() {
				return constants.getEmail();
			}

		
			@Override
			public String getProjectId() {
				return projectId;
			}

			
		};
	}
	
}