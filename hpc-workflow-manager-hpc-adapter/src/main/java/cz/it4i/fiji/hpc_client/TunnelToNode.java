package cz.it4i.fiji.hpc_client;

import java.io.Closeable;

public interface TunnelToNode extends Closeable{
	int getLocalPort();

	String getLocalHost();
}
