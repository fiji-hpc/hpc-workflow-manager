package cz.it4i.fiji.hpc_client;

import java.io.IOException;
import java.io.InputStream;

public interface UploadingFile {
	InputStream getInputStream() throws IOException;

	String getName();

	long getLength() throws IOException;

	long getLastTime();
}