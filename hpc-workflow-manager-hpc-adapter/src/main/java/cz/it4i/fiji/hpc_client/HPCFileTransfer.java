package cz.it4i.fiji.hpc_client;

import java.io.Closeable;
import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.util.List;

import cz.it4i.fiji.scpclient.TransferFileProgress;

public interface HPCFileTransfer extends Closeable {

	
	@Override
	void close();

	void upload(UploadingFile file) throws InterruptedIOException;

	void download(String files, Path workDirectory) throws InterruptedIOException;

	List<Long> obtainSize(List<String> files) throws InterruptedIOException;

	List<String> getContent(List<String> logs);
	
	void setProgress(TransferFileProgress progress);
}
