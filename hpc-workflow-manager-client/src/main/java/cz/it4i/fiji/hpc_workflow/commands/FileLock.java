package cz.it4i.fiji.hpc_workflow.commands;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileLock implements Closeable {

	public static final Logger log = LoggerFactory.getLogger(cz.it4i.fiji.hpc_workflow.commands.FileLock.class);

	private final Path localPath;

	private FileChannel fileChannel;

	private java.nio.channels.FileLock lock;

	public FileLock(Path lockPath) {
		this.localPath = lockPath;
	}

	public boolean tryLock() throws IOException {
		this.fileChannel = FileChannel.open(localPath, StandardOpenOption.READ, StandardOpenOption.WRITE,
				StandardOpenOption.CREATE);
		try {
			this.lock = fileChannel.tryLock();
			if (this.lock != null) {
				return true;
			}
		} catch (OverlappingFileLockException e) {
			// IGNORE
		}
		this.fileChannel.close();
		this.fileChannel = null;
		return false;
	}

	@Override
	public synchronized void close() {
		if (lock != null) {
			try {
				lock.release();
				lock = null;
			}
			catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		if (fileChannel != null) {
			try {
				fileChannel.close();
				fileChannel = null;
			}
			catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		try {
			if (localPath.toFile().exists()) {
				Files.delete(localPath);
			}
		}
		catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
}
