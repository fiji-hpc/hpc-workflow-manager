/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/

package cz.it4i.fiji.hpc_client;

import java.util.Collection;
import java.util.List;

import cz.it4i.fiji.scpclient.TransferFileProgress;

public interface HPCClient<T> {

	void checkConnection();

	long createJob(T jobSettings);

	void submitJob(long jobId);

	JobInfo obtainJobInfo(long jobId);

	void cancelJob(Long jobId);

	void deleteJob(long id);

	HPCFileTransfer startFileTransfer(long jobId, TransferFileProgress notifier);

	default HPCFileTransfer startFileTransfer(long jobId) {
		return startFileTransfer(jobId, Notifiers.emptyTransferFileProgress());
	}

	List<JobFileContent> downloadPartsOfJobFiles(Long jobId,
		List<SynchronizableFile> files);

	Collection<String> getChangedFiles(long jobId);

	HPCDataTransfer startDataTransfer(long jobId, int nodeNumber, int port);

}
