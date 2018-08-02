
package cz.it4i.fiji.haas_java_client;

import java.nio.file.Path;

public class FileTransferInfo {

	private final Path path;

	private final FileTransferState state;

	public FileTransferInfo(final Path path, final FileTransferState state) {
		this.path = path;
		this.state = state;
	}

	public String getFileNameAsString() {
		return path.getFileName().toString();
	}

	public FileTransferState getState() {
		return state;
	}

}
