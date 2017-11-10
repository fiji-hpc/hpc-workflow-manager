import java.io.IOException;
import java.nio.file.Paths;

import com.jcraft.jsch.JSchException;

import cz.it4i.fiji.scpclient.ScpClient;

public class TestSCP {

	public TestSCP() {
		
	}
	
	public static void main(String[] args) throws JSchException, IOException {
		try(ScpClient scp = new ScpClient("salomon.it4i.cz", "koz01", "/home/koz01/.ssh/it4i_rsa-np", null)) {
			System.out.println( scp.upload(Paths.get("/home/koz01/Work/vyzkumnik/fiji/work/aaa/spim-data/exampleSingleChannel(9).czi"), "'/home/koz01/exampleSingleChannel(9).czi'"));
		}
	}

}
