package cz.it4i.fiji.haas_java_client;

import cz.it4i.fiji.haas_java_client.proxy.JobFileContentExt;
import cz.it4i.fiji.haas_java_client.proxy.SynchronizableFilesExt;

class JobFileContentImpl implements JobFileContent{

	private final JobFileContentExt contentExt;

	public JobFileContentImpl(JobFileContentExt contentExt) {
		this.contentExt = contentExt;
	}

	@Override
	public String getContent() {
		return contentExt.getContent();
	}

	@Override
	public String getRelativePath() {
		return contentExt.getRelativePath();
	}

	@Override
	public Long getOffset() {
		return contentExt.getOffset();
	}

	@Override
	public SynchronizableFileType getFileType() {
		return getFileType(contentExt.getFileType());
	}

	@Override
	public Long getSubmittedTaskInfoId() {
		return contentExt.getSubmittedTaskInfoId();
	}

	private SynchronizableFileType getFileType(SynchronizableFilesExt fileType) {
		// TODO Auto-generated method stub
		return null;
	}
}
