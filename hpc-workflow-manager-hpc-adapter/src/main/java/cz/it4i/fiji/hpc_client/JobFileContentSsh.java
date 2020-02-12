
package cz.it4i.fiji.hpc_client;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JobFileContentSsh implements JobFileContent {

	private String content;

	private String relativePath;

	private long taskId;

	private long offset;

	private SynchronizableFileType fileType;

	@Override
	public String getContent() {
		return this.content;
	}

	@Override
	public SynchronizableFileType getFileType() {
		return this.fileType;
	}

	@Override
	public String getRelativePath() {
		return this.relativePath;
	}

	@Override
	public long getTaskId() {
		return this.taskId;
	}

	@Override
	public long getOffset() {
		return this.offset;
	}

}
