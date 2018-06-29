package cz.it4i.fiji.haas_java_client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UploadingFileData implements UploadingFile {

	private final byte[] data;
	private final String name;
	
	
	public UploadingFileData(String name, byte[] data) {
		this.data = data;
		this.name = name;
	}

	public UploadingFileData(String string) {
		this(string, new byte[0]);
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(data);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getLength() throws IOException {
		return data.length;
	}

	@Override
	public long getLastTime() {
		return 0;
	}

}
