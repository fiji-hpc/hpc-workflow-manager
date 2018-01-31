package cz.it4i.fiji.haas_java_client;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.Collection;

import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;

public interface HaaSFileTransfer extends Closeable {

	Collection<String> getChangedFiles();

	@Override
	void close();

	void upload(Iterable<UploadingFile> files);
	
	void download(Iterable<String> files, Path workDIrectory);

	
}
