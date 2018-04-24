package cz.it4i.fiji.haas.data_transfer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;

public class UploadingFileImpl implements UploadingFile{

	private final Path path;
	
	public UploadingFileImpl(Path path) {
		this.path = path;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return Files.newInputStream(path);
	}

	@Override
	public String getName() {
		return path.getFileName().toString();
	}

	@Override
	public long getLength() throws IOException {
		return Files.size(path);
	}

	@Override
	public long getLastTime() {
		// TODO Auto-generated method stub
		return 0;
	}

}
