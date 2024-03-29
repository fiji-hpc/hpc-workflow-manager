/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/

package cz.it4i.fiji.hpc_workflow;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import cz.it4i.fiji.hpc_client.JobState;
import cz.it4i.fiji.hpc_client.ProgressNotifier;
import cz.it4i.fiji.hpc_client.SynchronizableFileType;
import cz.it4i.fiji.hpc_client.data_transfer.FileTransferInfo;
import cz.it4i.fiji.hpc_workflow.core.JobType;

public interface WorkflowJob {

	void startJob(ProgressNotifier p) throws IOException;

	void cancelJob();

	boolean delete();

	JobState getState();

	void update();

	List<FileTransferInfo> getFileTransferInfo();

	List<String> getComputationOutput(
		List<SynchronizableFileType> types);

	default String getComputationOutput(final SynchronizableFileType type) {
		return getComputationOutput(Arrays.asList(type)).get(0);
	}

	List<Task> getTasks();

	List<String> getFileContents(List<String> files);

	JobType getJobType();

	Path getInputDirectory();

	Path getDirectory();

	Path getPathToLocalResultFile();

	String getPathToRemoteResultFile();

	Long getId();

	void startUpload();

	void stopUpload();

	boolean isUploaded();

	boolean isUploading();

	void setUploaded(boolean b);

	void setUploadNotifier(ProgressNotifier progress);

	boolean canBeUploaded();

	CompletableFuture<?> startDownload() throws IOException;

	void stopDownload();

	boolean isDownloaded();

	boolean isDownloading();

	void setDownloaded(boolean val);

	void setDownloadNotifier(ProgressNotifier downloadProgress);

	boolean canBeDownloaded();

	void resumeTransfer();

	CompletableFuture<JobState> getStateAsync(Executor executor);

	Comparator<? extends WorkflowJob> getComparator();

	String getEndTime();

	String getStartTime();

	String getCreationTime();

	String getJobTypeName();

	Path getOutputDirectory();

	void exploreErrors();

	boolean isVisibleInBDV();
}


