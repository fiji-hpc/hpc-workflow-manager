/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.haas;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import cz.it4i.fiji.hpc_client.HPCClient;
import cz.it4i.fiji.hpc_client.HPCDataTransfer;
import cz.it4i.fiji.hpc_client.HPCFileTransfer;
import cz.it4i.fiji.hpc_client.JobFileContent;
import cz.it4i.fiji.hpc_client.JobInfo;
import cz.it4i.fiji.hpc_client.SynchronizableFile;
import cz.it4i.fiji.hpc_client.TunnelToNode;
import cz.it4i.fiji.scpclient.TransferFileProgress;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class HPCClientProxyAdapter<T> implements HPCClient<T> {

	@AllArgsConstructor
	public static final class JobSubmission<T> {

		@Getter
		private final long jobId;

		@Getter
		private final T jobSettings;
	}

	private final Supplier<HPCClient<T>> clientSupplier;
	private final Supplier<T> jobSettingsSupplier;
	private HPCClient<T> hpcClient;


	public HPCClientProxyAdapter(Supplier<HPCClient<T>> clientSupplier,
		Supplier<T> jobSettingsSupplier)
	{
		this.clientSupplier = clientSupplier;
		this.jobSettingsSupplier = jobSettingsSupplier;
	}

	public JobSubmission<T> createJob() {
		T settings = jobSettingsSupplier.get();
		return new JobSubmission<>(createJob(settings), settings);
	}

	@Override
	public void checkConnection() {
		getHPCClient().checkConnection();
	}

	@Override
	public long createJob(T jobSettings) {
		return getHPCClient().createJob(jobSettings);
	}

	@Override
	public HPCFileTransfer startFileTransfer(long jobId,
		TransferFileProgress notifier)
	{
		return getHPCClient().startFileTransfer(jobId, notifier);
	}

	@Override
	public HPCFileTransfer startFileTransfer(long jobId) {
		return getHPCClient().startFileTransfer(jobId);
	}

	@Override
	public TunnelToNode openTunnel(long jobId, String nodeIP, int localPort,
		int remotePort)
	{
		return getHPCClient().openTunnel(jobId, nodeIP, localPort, remotePort);
	}

	@Override
	public void submitJob(long jobId) {
		getHPCClient().submitJob(jobId);
	}

	@Override
	public JobInfo obtainJobInfo(long jobId) {
		return getHPCClient().obtainJobInfo(jobId);
	}

	@Override
	public List<JobFileContent> downloadPartsOfJobFiles(Long jobId,
		List<SynchronizableFile> files)
	{
		return getHPCClient().downloadPartsOfJobFiles(jobId, files);
	}

	@Override
	public Collection<String> getChangedFiles(long jobId) {
		return getHPCClient().getChangedFiles(jobId);
	}

	@Override
	public void cancelJob(Long jobId) {
		getHPCClient().cancelJob(jobId);
	}

	@Override
	public void deleteJob(long id) {
		getHPCClient().deleteJob(id);
	}

	@Override
	public HPCDataTransfer startDataTransfer(long jobId, int nodeNumber,
		int port)
	{
		return getHPCClient().startDataTransfer(jobId, nodeNumber, port);
	}

	private synchronized HPCClient<T> getHPCClient() {
		if (hpcClient == null) {
			hpcClient = clientSupplier.get();
		}
		return hpcClient;
	}
}
