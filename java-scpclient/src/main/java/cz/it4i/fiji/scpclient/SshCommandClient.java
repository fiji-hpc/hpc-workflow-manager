
package cz.it4i.fiji.scpclient;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Identity;
import com.jcraft.jsch.JSchException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshCommandClient extends AbstractBaseSshClient {

	private final static Logger log = LoggerFactory.getLogger(
		SshCommandClient.class);

	public SshCommandClient(String hostName, String username,
		byte[] privateKeyFile) throws JSchException
	{
		super(hostName, username, privateKeyFile);
	}

	public SshCommandClient(String hostName, String username,
		Identity privateKeyFile) throws JSchException
	{
		super(hostName, username, privateKeyFile);
	}

	public SshCommandClient(String hostName, String userName, String keyFile,
		String pass) throws JSchException
	{
		super(hostName, userName, keyFile, pass);
	}

	public List<String> executeCommand(String command) {
		List<String> result = new LinkedList<>();
		try {
			ChannelExec channelExec = (ChannelExec) getConnectedSession().openChannel(
				"exec");

			InputStream in = channelExec.getInputStream();

			channelExec.setCommand(command);
			channelExec.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;

			while ((line = reader.readLine()) != null) {
				result.add(line);
			}

			int exitStatus = channelExec.getExitStatus();
			channelExec.disconnect();

			if (exitStatus < 0) {
				log.debug("Done, but exit status not set!");
			}
			else if (exitStatus > 0) {
				log.debug("Done, but with error!");
			}
			else {
				log.debug("Done!");
			}
		}
		catch (Exception e) {
			log.error("Error: ", e);
			throw new RuntimeException(e);
		}
		return result;
	}

	public boolean setPortForwarding(int lport, String rhost, int rport) {
		try {
			getConnectedSession().setPortForwardingL(lport, rhost, rport);
		}
		catch (JSchException exc) {
			log.error("forward", exc);
			return false;
		}
		return true;
	}
}
