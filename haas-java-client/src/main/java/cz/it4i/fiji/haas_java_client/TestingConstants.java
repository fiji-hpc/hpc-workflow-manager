package cz.it4i.fiji.haas_java_client;

interface TestingConstants {
	String USER_NAME = "testuser";
	String PASSWORD = "57f9caaf84";
	String EMAIL = "jan.kozusznik@vsb.cz";
	String PHONE = "999111000";
	
	static Settings getSettings(long templateId, int timeOut, long clusterNodeType, String projectId) {
		return new Settings() {
			
			@Override
			public String getUserName() {
				return USER_NAME;
			}
			
			@Override
			public String getPhone() {
				return PHONE;
			}
			
			@Override
			public String getPassword() {
				return PASSWORD;
			}
			
			@Override
			public String getEmail() {
				return EMAIL;
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