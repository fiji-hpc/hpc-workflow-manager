
package cz.it4i.fiji.hpc_workflow.core;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.data_transfer.PersistentSynchronizationProcess;
import cz.it4i.fiji.haas.ui.UpdatableObservableValue;
import cz.it4i.fiji.haas_java_client.FileTransferInfo;
import cz.it4i.fiji.haas_java_client.JobState;
import cz.it4i.fiji.haas_java_client.ProgressNotifier;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.hpc_workflow.Task;
import cz.it4i.fiji.hpc_workflow.WorkflowJob;
import cz.it4i.fiji.hpc_workflow.ui.NewJobController.WorkflowType;

public class ObservableHPCWorkflowJob extends
	UpdatableObservableValue<WorkflowJob> implements Closeable
{

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_workflow.core.ObservableHPCWorkflowJob.class);

	private final PTransferProgress downloadProgress = new PTransferProgress(
		getValue()::setDownloaded, getValue()::isDownloaded,
		getValue()::isDownloading);
	private final PTransferProgress uploadProgress = new PTransferProgress(
		getValue()::setUploaded, getValue()::isUploaded, getValue()::isUploading);
	private final Executor executor;

	private final HaasOutputObservableValueRegistry haasOutputRegistry;

	private final TaskObservableValueRegistry taskRegistry;

	private final SimpleObservableList<FileTransferInfo> fileTransferList;

	private WorkflowJob workflowkJob;

	public interface TransferProgress {

		public Long getRemainingMiliseconds();

		public boolean isDone();

		public boolean isWorking();

		public boolean isFailed();

		public Float getRemainingPercents();
	}

	public ObservableHPCWorkflowJob(WorkflowJob wrapped,
		Function<WorkflowJob, UpdateStatus> updateFunction,
		Function<WorkflowJob, Object> stateProvider, Executor executorUI)
	{
		super(wrapped, updateFunction, stateProvider);
		this.workflowkJob = wrapped;
		this.executor = executorUI;
		wrapped.setDownloadNotifier(downloadProgress);
		wrapped.setUploadNotifier(uploadProgress);
		wrapped.resumeTransfer();

		haasOutputRegistry = new HaasOutputObservableValueRegistry(getValue());
		taskRegistry = new TaskObservableValueRegistry(getValue());
		fileTransferList = new SimpleObservableList<>(wrapped
			.getFileTransferInfo());
	}

	public TransferProgress getDownloadProgress() {
		return downloadProgress;
	}

	public TransferProgress getUploadProgress() {
		return uploadProgress;
	}

	public void removed() {
		getValue().setDownloadNotifier(null);
		getValue().setUploadNotifier(null);
	}

	public SimpleObservableList<FileTransferInfo> getFileTransferList() {
		return fileTransferList;
	}

	public SimpleObservableValue<String> getObservableSnakemakeOutput(
		SynchronizableFileType type)
	{
		return haasOutputRegistry.getObservableOutput(type);
	}

	public SimpleObservableList<Task> getObservableTaskList() {
		return taskRegistry.getTaskList();
	}

	@Override
	public void close() {
		haasOutputRegistry.close();
		taskRegistry.close();
	}

	@Override
	protected void fireValueChangedEvent() {
		// Do not change the lambda function bellow to method reference!
		// JDK Bug: https://bugs.openjdk.java.net/browse/JDK-8139836
		executor.execute(() -> super.fireValueChangedEvent());
	}

	// -- Private classes --

	private class PTransferProgress implements TransferProgress,
		ProgressNotifier
	{

		private final BooleanSupplier doneStatusSupplier;
		private final Consumer<Boolean> doneStatusConsumer;
		private final BooleanSupplier workingSupplier;
		private Long start;
		private Long remainingMiliseconds;
		private Float remainingPercents;
		private boolean failed = false;

		public PTransferProgress(Consumer<Boolean> doneStatusConsumer,
			BooleanSupplier doneStatusSupplier, BooleanSupplier workingSupplier)
		{
			this.doneStatusConsumer = doneStatusConsumer;
			this.doneStatusSupplier = doneStatusSupplier;
			this.workingSupplier = workingSupplier;
		}

		@Override
		public synchronized void setCount(int count, int total) {
			if (total <= -1) {
				clearProgress();
			}
			else if (start != null) {
				long delta = System.currentTimeMillis() - start;
				remainingMiliseconds = (long) ((double) delta / count * (total -
					count));
				remainingPercents = (((float) total - count) / total * 100);
			}
			fireValueChangedEvent();
		}

		@Override
		public synchronized void addItem(Object item) {
			if (Objects.equals(item, PersistentSynchronizationProcess.FAILED_ITEM)) {
				failed = true;
				doneStatusConsumer.accept(false);
				reloadFileTransferList();
				fireValueChangedEvent();
			}
			else if (start == null) {
				setDone(false);
				clearProgress();
				start = System.currentTimeMillis();
				fireValueChangedEvent();
			}
		}

		@Override
		public synchronized void done() {
			remainingMiliseconds = 0l;
			remainingPercents = 0.f;
			start = null;
			fireValueChangedEvent();
		}

		@Override
		public synchronized boolean isWorking() {
			return workingSupplier.getAsBoolean();
		}

		@Override
		public boolean isFailed() {
			return failed;
		}

		@Override
		public synchronized Long getRemainingMiliseconds() {
			return remainingMiliseconds;
		}

		@Override
		public synchronized Float getRemainingPercents() {
			return remainingPercents;
		}

		@Override
		public void setItemCount(int count, int total) {

		}

		@Override
		public void itemDone(final Object item) {
			reloadFileTransferList();
		}

		@Override
		public void setTitle(String title) {

		}

		@Override
		public boolean isDone() {
			return doneStatusSupplier.getAsBoolean();
		}

		private void reloadFileTransferList() {
			fileTransferList.setAll(getValue().getFileTransferInfo());
		}

		private void clearProgress() {
			remainingMiliseconds = null;
			remainingPercents = null;
		}

		private void setDone(boolean val) {
			failed = false;
			doneStatusConsumer.accept(val);
		}
	}

	public List<String> getFileContents(List<String> files) {
		return workflowkJob.getFileContents(files);
	}

	public JobState getState() {
		return workflowkJob.getState();
	}

	public WorkflowType getWorkflowType() {
		return workflowkJob.getWorkflowType();
	}

	public Path getInputDirectory() {
		return workflowkJob.getInputDirectory();
	}

	public String getUserScriptName() {
		if (workflowkJob instanceof MacroWorkflowJob) {
			MacroWorkflowJob typeJob = (MacroWorkflowJob) workflowkJob;
			return typeJob.getUserScriptName();
		}
		throw new UnsupportedOperationException(
			"Script name is support by MacroWorkflowJob but actual job is " +
				workflowkJob.getClass());
	}
	
	public long getLastStartedTimestamp() {
		MacroWorkflowJob typeJob = (MacroWorkflowJob) workflowkJob;
		return typeJob.getLastStartedTimestamp();
	}

}
