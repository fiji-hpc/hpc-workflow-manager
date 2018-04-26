package cz.it4i.fiji.haas_java_client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;

public class UploadingFileImpl implements UploadingFile {

	public static final Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_java_client.UploadingFileImpl.class);

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
		try {
			return Files.getLastModifiedTime(path).toMillis();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return 0;
		}
	}

}
