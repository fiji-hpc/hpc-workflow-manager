
package cz.it4i.fiji.haas_java_client;

import com.jcraft.jsch.JSchException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.proxy.FileTransferMethodExt;
import cz.it4i.fiji.scpclient.ScpClient;
import cz.it4i.fiji.scpclient.TransferFileProgress;

class HaaSFileTransferImp implements HaaSFileTransfer {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_java_client.HaaSFileTransferImp.class);

	private final FileTransferMethodExt ft;
	private final ScpClient scpClient;
	private TransferFileProgress progress;
	
	public HaaSFileTransferImp(FileTransferMethodExt ft, ScpClient scpClient, TransferFileProgress progress) {
		this.ft = ft;
		this.scpClient = scpClient;
		this.progress = progress;
	}

	@Override
	public void close() {
		scpClient.close();
	}

	@Override
	public void upload(final UploadingFile file) throws InterruptedIOException {
		final String destFile = "'" + ft.getSharedBasepath() + "/" + file
			.getName() + "'";
		try (InputStream is = file.getInputStream()) {
			if (!scpClient.upload(is, destFile, file.getLength(), file.getLastTime(),
				progress))
			{
				throw new HaaSClientException("Uploading of " + file + " to " +
					destFile + " failed");
			}
		}
		catch (final InterruptedIOException e) {
			throw e;
		}
		catch (JSchException | IOException e) {
			throw new HaaSClientException(e);
		}
	}

	@Override
	public void download(String fileName, final Path workDirectory)
		throws InterruptedIOException
	{
		try {
			fileName = fileName.replaceFirst("/", "");
			final Path rFile = workDirectory.resolve(fileName);
			final String fileToDownload = "'" + ft.getSharedBasepath() + "/" +
				fileName + "'";
			if (!scpClient.download(fileToDownload, rFile, progress)) {
				throw new HaaSClientException("Downloading of " + fileName + " to " +
					workDirectory + " failed");
			}
		}
		catch (final InterruptedIOException e) {
			throw e;
		}
		catch (JSchException | IOException e) {
			throw new HaaSClientException(e);
		}
	}

	@Override
	public void setProgress(TransferFileProgress progress) {
		this.progress = progress;
	}
	
	@Override
	public List<Long> obtainSize(List<String> files) throws InterruptedIOException {
		try {
			return getSizes(files.stream()
					.map(filename -> "'" + ft.getSharedBasepath() + "/" + filename + "'").collect(Collectors.toList()));
		} catch (InterruptedIOException e) {
			throw e;
		} catch (JSchException | IOException e) {
			throw new HaaSClientException(e);
		}

	}

	// FIXME: merge with download - stream provider for file, consumer for stream
	@Override
	public List<String> getContent(List<String> files) {
		List<String> result = new LinkedList<>();
		try {
			for (String fileName : files) {
				fileName = replaceIfFirstFirst(fileName);
				try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
					String fileToDownload = "'" + ft.getSharedBasepath() + "/" + fileName + "'";
					scpClient.download(fileToDownload, os, progress);
					os.flush();
					result.add(os.toString());
				}
			}
		} catch (JSchException | IOException e) {
			throw new HaaSClientException(e);
		}
		return result;
	}
	
	private String replaceIfFirstFirst(String fileName) {
		if (fileName.length() < 0 && fileName.charAt(0) == '/') {
			fileName = fileName.substring(1);
		}
		return fileName;
	}
	
	private List<Long> getSizes(List<String> asList) throws JSchException, IOException {
		List<Long> result = new LinkedList<>();
		for (String lfile : asList) {
			result.add(scpClient.size(lfile));
		}
		return result;
	}

}
