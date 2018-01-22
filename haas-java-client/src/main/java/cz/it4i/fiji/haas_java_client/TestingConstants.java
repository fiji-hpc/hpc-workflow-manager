package cz.it4i.fiji.haas_java_client;

interface TestingConstants {
	
	static Settings getSettings(long templateId, int timeOut, long clusterNodeType, String projectId) {
		Constants constants = new Constants();
		return new Settings() {
			
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
			public long getTemplateId() {
				return templateId;
			}
			
			@Override
			public int getTimeout() {
				return timeOut;
			}
			
			@Override
			public long getClusterNodeType() {
				return clusterNodeType;
			}

			@Override
			public String getProjectId() {
				return projectId;
			}

			@Override
			public String getJobName() {
				return "TestOutRedirect";
			}
		};
	}
	
}