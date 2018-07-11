package cz.it4i.fiji.haas_java_client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.proxy.JobManagementWs;
import cz.it4i.fiji.haas_java_client.proxy.JobManagementWsSoap;
import cz.it4i.fiji.haas_java_client.proxy.PasswordCredentialsExt;
import cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt;
import cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWs;
import cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap;

public class GetJobInfo {

	public static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_java_client.GetJobInfo.class);

	public static void main(String[] args) {
		HaaSClientSettings settings = SettingsProvider.getSettings("OPEN-12-20", TestingConstants.CONFIGURATION_FILE_NAME);
		HaaSClient client = new HaaSClient(settings);
		JobInfo ji = client.obtainJobInfo(334);
		System.out.println("created: " + ji.getCreationTime());
		JobManagementWsSoap ws = new JobManagementWs().getJobManagementWsSoap();
		UserAndLimitationManagementWsSoap wsuser = new UserAndLimitationManagementWs().getUserAndLimitationManagementWsSoap();
		PasswordCredentialsExt ps = new PasswordCredentialsExt();
		ps.setUsername(settings.getUserName());
		ps.setPassword(settings.getPassword());
		String session = wsuser.authenticateUserPassword(ps);
		SubmittedJobInfoExt info = ws.getCurrentInfoForJob(334, session);
		System.out.println("created: " + info.getCreationTime().toGregorianCalendar());
		
	}

}
