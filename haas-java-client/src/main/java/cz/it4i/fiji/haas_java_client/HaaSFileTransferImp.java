package cz.it4i.fiji.haas_java_client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;

import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;
import cz.it4i.fiji.haas_java_client.proxy.FileTransferMethodExt;
import cz.it4i.fiji.scpclient.ScpClient;
import cz.it4i.fiji.scpclient.TransferFileProgress;

class HaaSFileTransferImp implements HaaSFileTransfer {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_java_client.HaaSFileTransferImp.class);

	private FileTransferMethodExt ft;
	private ScpClient scpClient;
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
	public void upload(UploadingFile file) {
		String destFile = "'" + ft.getSharedBasepath() + "/" + file.getName() + "'";
		try (InputStream is = file.getInputStream()) {
			boolean result = scpClient.upload(is, destFile, file.getLength(), file.getLastTime(), progress);
			if (!result) {
				throw new HaaSClientException("Uploading of " + file + " to " + destFile + " failed");
			}
		} catch (JSchException | IOException e) {
			throw new HaaSClientException();
		}
	}

	@Override
	public void download(String fileName, Path workDirectory) {
		try {
			fileName = fileName.replaceFirst("/", "");
			Path rFile = workDirectory.resolve(fileName);
			String fileToDownload = "'" + ft.getSharedBasepath() + "/" + fileName + "'";
			scpClient.download(fileToDownload, rFile, progress);
		} catch (JSchException | IOException e) {
			throw new HaaSClientException(e);
		}
	}
	
	@Override
	public void setProgress(TransferFileProgress progress) {
		this.progress = progress;
	}
	
	/*
	@Override
	public void download(Iterable<String> files, Path workDirectory) {
		List<Long> fileSizes;
		try {
			fileSizes = HaaSClient.getSizes(StreamSupport.stream(files.spliterator(), false)
					.map(filename -> "'" + ft.getSharedBasepath() + "/" + filename + "'").collect(Collectors.toList()),
					scpClient);

			final long totalFileSize = fileSizes.stream().mapToLong(i -> i.longValue()).sum();
			TransferFileProgressForHaaSClient progress = new TransferFileProgressForHaaSClient(totalFileSize,
					HaaSClient.DUMMY_PROGRESS_NOTIFIER);
			int idx = 0;
			for (String fileName : files) {
				fileName = fileName.replaceFirst("/", "");
				Path rFile = workDirectory.resolve(fileName);
				String fileToDownload = "'" + ft.getSharedBasepath() + "/" + fileName + "'";
				String item;
				progress.addItem(item = fileName);
				progress.startNewFile(fileSizes.get(idx));
				scpClient.download(fileToDownload, rFile, progress);
				progress.itemDone(item);
				idx++;
			}
		} catch (JSchException | IOException e) {
			throw new HaaSClientException(e);
		}
	}
	*/

	@Override
	public List<Long> obtainSize(List<String> files) {
		try {
			return getSizes(files.stream()
					.map(filename -> "'" + ft.getSharedBasepath() + "/" + filename + "'").collect(Collectors.toList()));
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
				fileName = replaceIfFirstFirst(fileName, "/", "");
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

	private String replaceIfFirstFirst(String fileName, String string, String string2) {
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
