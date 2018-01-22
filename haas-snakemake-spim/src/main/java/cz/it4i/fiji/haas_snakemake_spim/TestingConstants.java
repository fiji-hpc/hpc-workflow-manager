package cz.it4i.fiji.haas_snakemake_spim;

import cz.it4i.fiji.haas_java_client.Settings;

public interface TestingConstants {
	
	static Settings getSettings() {
		Constants constants = new Constants();
		long templateId = 2l;
		int timeOut = 9600; 
		long clusterNodeType = 6l;
		
	
		return new Settings() {
			
			@Override
			public String getUserName() {
				return constants.getUserName();
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
				return constants.getProjectId();
			}

			@Override
			public String getJobName() {
				return "HaaS-Snakemake-SPIM";
			}
		};
	}
	
}