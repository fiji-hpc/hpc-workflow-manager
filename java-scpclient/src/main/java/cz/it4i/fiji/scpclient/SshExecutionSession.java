
package cz.it4i.fiji.scpclient;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface SshExecutionSession extends Closeable {

	InputStream getStdout() throws IOException;

	InputStream getStderr() throws IOException;

	int getExitStatus();

	@Override
	void close();
}
