package cz.it4i.fiji.haas_java_client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;

import cz.it4i.fiji.haas_java_client.HaaSClient.P_ProgressNotifierDecorator4Size;
import cz.it4i.fiji.haas_java_client.HaaSClient.UploadingFile;
import cz.it4i.fiji.haas_java_client.proxy.FileTransferMethodExt;
import cz.it4i.fiji.scpclient.ScpClient;

class HaaSFileTransferImp implements HaaSFileTransfer {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_java_client.HaaSFileTransferImp.class);

	private FileTransferMethodExt ft;
	private ScpClient scpClient;
	private ProgressNotifier notifier;

	public HaaSFileTransferImp(FileTransferMethodExt ft, ScpClient scpClient, ProgressNotifier notifier) {
		this.ft = ft;
		this.scpClient = scpClient;
		this.notifier = notifier;
	}

	@Override
	public void close() {
		scpClient.close();
	}

	@Override
	public void upload(Iterable<UploadingFile> files) {
		List<Long> totalSizes = StreamSupport.stream(files.spliterator(), false).map(f -> f.getLength())
				.collect(Collectors.toList());
		long totalSize = totalSizes.stream().mapToLong(l -> l.longValue()).sum();
		TransferFileProgressForHaaSClient progress = new TransferFileProgressForHaaSClient(totalSize, notifier);
		int index = 0;
		for (UploadingFile file : files) {
			String item;
			progress.startNewFile(totalSizes.get(index));
			notifier.addItem(item = "Uploading file: " + file.getName());
			String destFile = "'" + ft.getSharedBasepath() + "/" + file.getName() + "'";
			try (InputStream is = file.getInputStream()) {
				boolean result = scpClient.upload(is, destFile, file.getLength(), file.getLastTime(), progress);
				notifier.itemDone(item);
				if (!result) {
					throw new HaaSClientException("Uploading of " + file + " to " + destFile + " failed");
				}
			} catch (JSchException | IOException e) {
				throw new HaaSClientException();
			}
			index++;
		}

	}

	@Override
	public void download(Iterable<String> files, Path workDirectory) {
		List<Long> fileSizes;
		try {
			fileSizes = HaaSClient.getSizes(StreamSupport.stream(files.spliterator(), false)
					.map(filename -> "'" + ft.getSharedBasepath() + "/" + filename + "'").collect(Collectors.toList()),
					scpClient, new P_ProgressNotifierDecorator4Size(notifier));

			final long totalFileSize = fileSizes.stream().mapToLong(i -> i.longValue()).sum();
			TransferFileProgressForHaaSClient progress = new TransferFileProgressForHaaSClient(totalFileSize, notifier);
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

	@Override
	public List<Long> obtainSize(List<String> files) {
		try {
			return HaaSClient.getSizes(files.stream()
					.map(filename -> "'" + ft.getSharedBasepath() + "/" + filename + "'").collect(Collectors.toList()),
					scpClient, notifier);
		} catch (JSchException | IOException e) {
			throw new HaaSClientException(e);
		}

	}

	// TASK merge with download - stream provider for file, consumer for stream
	@Override
	public List<String> getContent(List<String> files) {
		List<String> result = new LinkedList<>();
		List<Long> fileSizes;
		try {
			fileSizes = HaaSClient.getSizes(StreamSupport.stream(files.spliterator(), false)
					.map(filename -> "'" + ft.getSharedBasepath() + "/" + filename + "'").collect(Collectors.toList()),
					scpClient, new P_ProgressNotifierDecorator4Size(notifier));

			final long totalFileSize = fileSizes.stream().mapToLong(i -> i.longValue()).sum();
			TransferFileProgressForHaaSClient progress = new TransferFileProgressForHaaSClient(totalFileSize, notifier);
			int idx = 0;
			for (String fileName : files) {
				fileName = replaceIfFirstFirst(fileName, "/", "");
				try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
					String fileToDownload = "'" + ft.getSharedBasepath() + "/" + fileName + "'";
					String item;
					progress.addItem(item = fileName);
					progress.startNewFile(fileSizes.get(idx));
					scpClient.download(fileToDownload, os, progress);
					os.flush();
					progress.itemDone(item);
					idx++;
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

}
