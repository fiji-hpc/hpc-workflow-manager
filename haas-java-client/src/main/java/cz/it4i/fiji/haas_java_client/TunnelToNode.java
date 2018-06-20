package cz.it4i.fiji.haas_java_client;

import java.io.Closeable;

public interface TunnelToNode extends Closeable{
	int getLocalPort();

	String getLocalHost();
}
