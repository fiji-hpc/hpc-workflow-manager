package cz.it4i.fiji.haas_java_client;

public interface JobFileContent {

	/**
	 * Gets the content value for this JobFileContentExt.
	 * 
	 * @return content
	 */
	java.lang.String getContent();

	/**
	 * Gets the relativePath value for this JobFileContentExt.
	 * 
	 * @return relativePath
	 */
	java.lang.String getRelativePath();

	/**
	 * Gets the offset value for this JobFileContentExt.
	 * 
	 * @return offset
	 */
	java.lang.Long getOffset();

	/**
	 * Gets the fileType value for this JobFileContentExt.
	 * 
	 * @return fileType
	 */
	SynchronizableFileType getFileType();

	/**
	 * Gets the submittedTaskInfoId value for this JobFileContentExt.
	 * 
	 * @return submittedTaskInfoId
	 */
	java.lang.Long getSubmittedTaskInfoId();

}
