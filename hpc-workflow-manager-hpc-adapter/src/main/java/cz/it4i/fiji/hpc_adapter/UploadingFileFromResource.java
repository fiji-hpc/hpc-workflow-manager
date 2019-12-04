package cz.it4i.fiji.hpc_adapter;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

import cz.it4i.fiji.hpc_client.UploadingFile;

public class UploadingFileFromResource implements UploadingFile {

	private final String fileName;
	
	private final String base;
	
	private final long lastTime;
	
	private Long length;
	
	public UploadingFileFromResource(String base, String fileName) {
		this.base = base;
		this.fileName = fileName;
		this.lastTime = Instant.now().getEpochSecond()*1000;
	}
	
	@Override
	public InputStream getInputStream() {
		return this.getClass().getResourceAsStream(base + "/" + fileName);
	}

	@Override
	public String getName() {
		return fileName;
	}

	@Override
	public long getLength() {
		if(length == null) {
			length = computeLenght();
		}
		return length;
	}

	private Long computeLenght() {
		try(InputStream is = getInputStream()) {
			long result = 0;
			int available;
			while(0 != (available = is.available())) {
				result += is.skip(available);
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLastTime() {
		return lastTime;
	}

}
