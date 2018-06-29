package cz.it4i.fiji.haas_java_client;

import java.io.Closeable;

public interface HaaSDataTransfer extends Closeable{
	void write(byte []buffer);
	byte[] read();
	void closeConnection();
}
