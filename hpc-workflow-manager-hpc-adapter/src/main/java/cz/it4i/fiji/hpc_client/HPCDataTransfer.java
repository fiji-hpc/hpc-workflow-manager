package cz.it4i.fiji.hpc_client;

import java.io.Closeable;

public interface HPCDataTransfer extends Closeable{
	void write(byte []buffer);
	byte[] read();
	void closeConnection();
}
