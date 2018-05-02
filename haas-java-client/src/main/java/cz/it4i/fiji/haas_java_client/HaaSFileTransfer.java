package cz.it4i.fiji.haas_java_client;

import java.io.Closeable;
import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.util.List;

import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;
import cz.it4i.fiji.scpclient.TransferFileProgress;

public interface HaaSFileTransfer extends Closeable {

	
	@Override
	void close();

	void upload(UploadingFile files) throws InterruptedIOException;

	void download(String files, Path workDirectory) throws InterruptedIOException;

	List<Long> obtainSize(List<String> files) throws InterruptedIOException;

	List<String> getContent(List<String> logs);
	
	void setProgress(TransferFileProgress progress);
}
