package cz.it4i.fiji.haas_java_client;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.rpc.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;

import cz.it4i.fiji.haas_java_client.proxy.CommandTemplateParameterValueExt;
import cz.it4i.fiji.haas_java_client.proxy.EnvironmentVariableExt;
import cz.it4i.fiji.haas_java_client.proxy.FileTransferMethodExt;
import cz.it4i.fiji.haas_java_client.proxy.FileTransferWsLocator;
import cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap;
import cz.it4i.fiji.haas_java_client.proxy.JobFileContentExt;
import cz.it4i.fiji.haas_java_client.proxy.JobManagementWsLocator;
import cz.it4i.fiji.haas_java_client.proxy.JobManagementWsSoap;
import cz.it4i.fiji.haas_java_client.proxy.JobPriorityExt;
import cz.it4i.fiji.haas_java_client.proxy.JobSpecificationExt;
import cz.it4i.fiji.haas_java_client.proxy.JobStateExt;
import cz.it4i.fiji.haas_java_client.proxy.PasswordCredentialsExt;
import cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt;
import cz.it4i.fiji.haas_java_client.proxy.SynchronizableFilesExt;
import cz.it4i.fiji.haas_java_client.proxy.TaskFileOffsetExt;
import cz.it4i.fiji.haas_java_client.proxy.TaskSpecificationExt;
import cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsLocator;
import cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWsSoap;
import cz.it4i.fiji.scpclient.ScpClient;
import cz.it4i.fiji.scpclient.TransferFileProgress;

public class HaaSClient {

	public static final TransferFileProgress DUMMY_TRANSFER_FILE_PROGRESS = new TransferFileProgress() {
		
		@Override
		public void dataTransfered(long bytesTransfered) {
			// TODO Auto-generated method stub
			
		}
	};

	public static ProgressNotifier DUMMY_PROGRESS_NOTIFIER = new ProgressNotifier() {
	
		@Override
		public void setTitle(String title) {
		}
	
		@Override
		public void setItemCount(int count, int total) {
		}
	
		@Override
		public void setCount(int count, int total) {
		}
	
		@Override
		public void itemDone(Object item) {
		}
	
		@Override
		public void done() {
		}
	
		@Override
		public void addItem(Object item) {
		}
	};

	public static UploadingFile getUploadingFile(Path file) {
		return new UploadingFile() {

			@Override
			public InputStream getInputStream() {
				try {
					return Files.newInputStream(file);
				} catch (IOException e) {
					log.error(e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}

			@Override
			public String getName() {
				return file.getFileName().toString();
			}

			@Override
			public long getLength() {
				try {
					return Files.size(file);
				} catch (IOException e) {
					log.error(e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}

			@Override
			public long getLastTime() {
				try {
					return Files.getLastModifiedTime(file).toMillis();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}

		};
	}

	static public class SynchronizableFiles {

		private final Collection<TaskFileOffsetExt> files = new LinkedList<>();

		public void addFile(long taskId, SynchronizableFileType type, long offset) {
			TaskFileOffsetExt off = new TaskFileOffsetExt();
			off.setFileType(getType(type));
			off.setSubmittedTaskInfoId(taskId);
			off.setOffset(offset);
			files.add(off);
		}

		private Collection<TaskFileOffsetExt> getFiles() {
			return files;
		}

		private SynchronizableFilesExt getType(SynchronizableFileType type) {
			switch (type) {
			case LogFile:
				return SynchronizableFilesExt.LogFile;
			case ProgressFile:
				return SynchronizableFilesExt.ProgressFile;
			case StandardErrorFile:
				return SynchronizableFilesExt.StandardErrorFile;
			case StandardOutputFile:
				return SynchronizableFilesExt.StandardOutputFile;
			default:
				throw new UnsupportedOperationException("Unsupported type: " + type);
			}

		}
	}

	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_java_client.HaaSClient.class);

	final static private Map<JobStateExt, JobState> WS_STATE2STATE;

	static {
		Map<JobStateExt, JobState> map = new HashMap<JobStateExt, JobState>();
		map.put(JobStateExt.Canceled, JobState.Canceled);
		map.put(JobStateExt.Configuring, JobState.Configuring);
		map.put(JobStateExt.Failed, JobState.Failed);
		map.put(JobStateExt.Finished, JobState.Finished);
		map.put(JobStateExt.Queued, JobState.Queued);
		map.put(JobStateExt.Running, JobState.Running);
		map.put(JobStateExt.Submitted, JobState.Submitted);
		WS_STATE2STATE = Collections.unmodifiableMap(map);
	}
	
	private String sessionID;

	private UserAndLimitationManagementWsSoap userAndLimitationManagement;

	private JobManagementWsSoap jobManagement;

	private FileTransferWsSoap fileTransfer;

	private final Integer timeOut;

	private final Long templateId;

	private final Long clusterNodeType;

	private final String projectId;

	private final Map<Long, P_FileTransferPool> filetransferPoolMap = new HashMap<>();

	
	private final Settings settings;

	private final int numberOfNodes;

	public HaaSClient(Settings settings) {
		this.settings = settings;
		this.templateId = settings.getTemplateId();
		this.timeOut = settings.getTimeout();
		this.clusterNodeType = settings.getClusterNodeType();
		this.projectId = settings.getProjectId();
		this.numberOfNodes = settings.getNumberOfNodes();
	}

	public long createJob(String name, Collection<Entry<String, String>> templateParameters) {
		try {
			return doCreateJob(name, templateParameters);
		} catch (RemoteException | ServiceException e) {
			throw new RuntimeException(e);
		}
	}

	public HaaSFileTransfer startFileTransfer(long jobId, TransferFileProgress notifier) {
		try {
			return getFileTransferMethod(jobId, notifier);
		} catch (RemoteException | ServiceException | UnsupportedEncodingException | JSchException e) {
			throw new HaaSClientException(e);
		}
	}

	public void submitJob(long jobId) {
		try {
			doSubmitJob(jobId);
		} catch (RemoteException | ServiceException e) {
			throw new HaaSClientException(e);
		}
	}

	public JobInfo obtainJobInfo(long jobId) {
		try {
			final SubmittedJobInfoExt info = getJobManagement().getCurrentInfoForJob(jobId, getSessionID());
			final Collection<Long> tasksId = Arrays.asList(info.getTasks()).stream().map(ti -> ti.getId())
					.collect(Collectors.toList());
			return new JobInfo() {
				@Override
				public Collection<Long> getTasks() {
					return tasksId;
				}

				@Override
				public JobState getState() {
					return WS_STATE2STATE.get(info.getState());
				}

				@Override
				public java.util.Calendar getStartTime() {
					return info.getStartTime();
				};

				@Override
				public java.util.Calendar getEndTime() {
					return info.getEndTime();
				};

				@Override
				public Calendar getCreationTime() {
					return info.getCreationTime();
				};
			};
		} catch (RemoteException | ServiceException e) {
			throw new HaaSClientException(e);
		}

	}

	public Collection<JobFileContentExt> downloadPartsOfJobFiles(Long jobId, HaaSClient.SynchronizableFiles files) {
		try {
			return Arrays.asList(getFileTransfer().downloadPartsOfJobFilesFromCluster(jobId,
					files.getFiles().stream().toArray(TaskFileOffsetExt[]::new), getSessionID()));
		} catch (RemoteException | ServiceException e) {
			throw new HaaSClientException(e);
		}
	}

	public Collection<String> getChangedFiles(long jobId) {
		try {
			return Arrays.asList(getFileTransfer().listChangedFilesForJob(jobId, getSessionID()));
		} catch (RemoteException | ServiceException e) {
			throw new HaaSClientException(e);
		}
	}

	public void cancelJob(Long jobId) {
		try {
			getJobManagement().cancelJob(jobId, getSessionID());
		} catch (RemoteException | ServiceException e) {
			throw new HaaSClientException(e);
		}
	}

	private HaaSFileTransferImp getFileTransferMethod(long jobId, TransferFileProgress progress)
			throws RemoteException, UnsupportedEncodingException, ServiceException, JSchException {
		P_FileTransferPool pool = filetransferPoolMap.computeIfAbsent(jobId, id -> new P_FileTransferPool(id));
		FileTransferMethodExt ft = pool.obtain();
		try {
			return new HaaSFileTransferImp(ft, getScpClient(ft), progress) {
				@Override
				public void close() {
					super.close();
					try {
						pool.release();
					} catch (RemoteException | ServiceException e) {
						throw new HaaSClientException(e);
					}
				};
			};
		} catch (UnsupportedEncodingException | JSchException e) {
			pool.release();
			throw e;
		}
	}

	private void doSubmitJob(long jobId) throws RemoteException, ServiceException {
		getJobManagement().submitJob(jobId, getSessionID());
	}

	private long doCreateJob(String name, Collection<Entry<String, String>> templateParameters)
			throws RemoteException, ServiceException {
		Collection<TaskSpecificationExt> taskSpec = IntStream.range(0, numberOfNodes)
				.mapToObj(index -> createTaskSpecification(name + ": " + index, templateId, templateParameters))
				.collect(Collectors.toList());
		JobSpecificationExt jobSpecification = createJobSpecification(name, taskSpec);
		SubmittedJobInfoExt job = getJobManagement().createJob(jobSpecification, getSessionID());
		return job.getId();
	}

	private ScpClient getScpClient(FileTransferMethodExt fileTransfer)
			throws UnsupportedEncodingException, JSchException {
		byte[] pvtKey = fileTransfer.getCredentials().getPrivateKey().getBytes("UTF-8");
		return new ScpClient(fileTransfer.getServerHostname(), fileTransfer.getCredentials().getUsername(), pvtKey);
	}

	private JobSpecificationExt createJobSpecification(String name, Collection<TaskSpecificationExt> tasks) {
		JobSpecificationExt testJob = new JobSpecificationExt();
		testJob.setName(name);
		testJob.setMinCores(1);
		testJob.setMaxCores(1);
		testJob.setPriority(JobPriorityExt.Average);
		testJob.setProject(projectId);
		testJob.setWaitingLimit(null);
		testJob.setWalltimeLimit(timeOut);
		testJob.setNotificationEmail(settings.getEmail());
		testJob.setPhoneNumber(settings.getPhone());
		testJob.setNotifyOnAbort(false);
		testJob.setNotifyOnFinish(false);
		testJob.setNotifyOnStart(false);
		testJob.setClusterNodeTypeId(clusterNodeType);
		testJob.setEnvironmentVariables(new EnvironmentVariableExt[0]);
		testJob.setTasks(tasks.stream().toArray(TaskSpecificationExt[]::new));
		
		return testJob;
	}

	private TaskSpecificationExt createTaskSpecification(String name, long templateId,
			Collection<Entry<String, String>> templateParameters) {

		TaskSpecificationExt testTask = new TaskSpecificationExt();
		testTask.setName(name);
		testTask.setMinCores(1);
		testTask.setMaxCores(1);
		testTask.setWalltimeLimit(timeOut);
		testTask.setRequiredNodes(null);
		testTask.setIsExclusive(false);
		testTask.setIsRerunnable(false);
		testTask.setStandardInputFile(null);
		testTask.setStandardOutputFile("console_Stdout");
		testTask.setStandardErrorFile("console_Stderr");
		testTask.setProgressFile("console_Stdprog");
		testTask.setLogFile("console_Stdlog");
		testTask.setClusterTaskSubdirectory(null);
		testTask.setCommandTemplateId(templateId); // commandTemplateID
		testTask.setEnvironmentVariables(new EnvironmentVariableExt[0]);
		testTask.setDependsOn(null);
		testTask.setTemplateParameterValues(templateParameters.stream()
				.map(pair -> new CommandTemplateParameterValueExt(pair.getKey(), pair.getValue()))
				.toArray(CommandTemplateParameterValueExt[]::new));
		return testTask;
	}

	private String authenticate() throws RemoteException, ServiceException {
		return getUserAndLimitationManagement()
				.authenticateUserPassword(new PasswordCredentialsExt(settings.getUserName(), settings.getPassword()));
	}

	private UserAndLimitationManagementWsSoap getUserAndLimitationManagement() throws ServiceException {
		if (userAndLimitationManagement == null) {
			userAndLimitationManagement = new UserAndLimitationManagementWsLocator()
					.getUserAndLimitationManagementWsSoap12();
		}
		return userAndLimitationManagement;
	}

	private JobManagementWsSoap getJobManagement() throws ServiceException {
		if (jobManagement == null) {
			jobManagement = new JobManagementWsLocator().getJobManagementWsSoap();
		}
		return jobManagement;
	}

	private FileTransferWsSoap getFileTransfer() throws ServiceException {
		if (fileTransfer == null) {
			fileTransfer = new FileTransferWsLocator().getFileTransferWsSoap12();
		}
		return fileTransfer;
	}

	private String getSessionID() throws RemoteException, ServiceException {
		if (sessionID == null) {
			sessionID = authenticate();
		}
		return sessionID;
	}

	public static class P_ProgressNotifierDecorator4Size extends P_ProgressNotifierDecorator {

		private static final int SIZE_RATIO = 20;

		public P_ProgressNotifierDecorator4Size(ProgressNotifier notifier) {
			super(notifier);

		}

		@Override
		public void setItemCount(int count, int total) {
			super.setItemCount(count, total);
			setCount(count, total * SIZE_RATIO);
		}
	}

	public static class P_ProgressNotifierDecorator implements ProgressNotifier {
		private final ProgressNotifier notifier;

		public P_ProgressNotifierDecorator(ProgressNotifier notifier) {
			this.notifier = notifier;
		}

		@Override
		public void setTitle(String title) {
			notifier.setTitle(title);
		}

		@Override
		public void setCount(int count, int total) {
			notifier.setCount(count, total);
		}

		@Override
		public void addItem(Object item) {
			notifier.addItem(item);
		}

		@Override
		public void setItemCount(int count, int total) {
			notifier.setItemCount(count, total);
		}

		@Override
		public void itemDone(Object item) {
			notifier.itemDone(item);
		}

		@Override
		public void done() {
			notifier.done();
		}
	}

	private interface P_Supplier<T> {

		T get() throws RemoteException, ServiceException;
	}

	private interface P_Consumer<T> {

		void accept(T val) throws RemoteException, ServiceException;
	}

	private class P_FileTransferPool {
		private FileTransferMethodExt holded;
		private int counter;
		private final P_Supplier<FileTransferMethodExt> factory;
		private final P_Consumer<FileTransferMethodExt> destroyer;

		public P_FileTransferPool(long jobId) {
			this.factory = () -> getFileTransfer().getFileTransferMethod(jobId, getSessionID());
			this.destroyer = val -> getFileTransfer().endFileTransfer(jobId, val, sessionID);
		}

		public synchronized FileTransferMethodExt obtain() throws RemoteException, ServiceException {
			if (holded == null) {
				holded = factory.get();
			}
			counter++;
			return holded;
		}

		public synchronized void release() throws RemoteException, ServiceException {
			if (--counter == 0) {
				destroyer.accept(holded);
				holded = null;
			}
		}

	}

}
