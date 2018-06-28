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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.rpc.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;

import cz.it4i.fiji.haas_java_client.proxy.ArrayOfCommandTemplateParameterValueExt;
import cz.it4i.fiji.haas_java_client.proxy.ArrayOfEnvironmentVariableExt;
import cz.it4i.fiji.haas_java_client.proxy.ArrayOfTaskFileOffsetExt;
import cz.it4i.fiji.haas_java_client.proxy.ArrayOfTaskSpecificationExt;
import cz.it4i.fiji.haas_java_client.proxy.CommandTemplateParameterValueExt;
import cz.it4i.fiji.haas_java_client.proxy.FileTransferMethodExt;
import cz.it4i.fiji.haas_java_client.proxy.FileTransferWs;
import cz.it4i.fiji.haas_java_client.proxy.FileTransferWsSoap;
import cz.it4i.fiji.haas_java_client.proxy.JobFileContentExt;
import cz.it4i.fiji.haas_java_client.proxy.JobManagementWs;
import cz.it4i.fiji.haas_java_client.proxy.JobManagementWsSoap;
import cz.it4i.fiji.haas_java_client.proxy.JobPriorityExt;
import cz.it4i.fiji.haas_java_client.proxy.JobSpecificationExt;
import cz.it4i.fiji.haas_java_client.proxy.JobStateExt;
import cz.it4i.fiji.haas_java_client.proxy.PasswordCredentialsExt;
import cz.it4i.fiji.haas_java_client.proxy.SubmittedJobInfoExt;
import cz.it4i.fiji.haas_java_client.proxy.SynchronizableFilesExt;
import cz.it4i.fiji.haas_java_client.proxy.TaskFileOffsetExt;
import cz.it4i.fiji.haas_java_client.proxy.TaskSpecificationExt;
import cz.it4i.fiji.haas_java_client.proxy.UserAndLimitationManagementWs;
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
				return SynchronizableFilesExt.LOG_FILE;
			case ProgressFile:
				return SynchronizableFilesExt.PROGRESS_FILE;
			case StandardErrorFile:
				return SynchronizableFilesExt.STANDARD_ERROR_FILE;
			case StandardOutputFile:
				return SynchronizableFilesExt.STANDARD_OUTPUT_FILE;
			default:
				throw new UnsupportedOperationException("Unsupported type: " + type);
			}

		}
	}

	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_java_client.HaaSClient.class);

	final static private Map<JobStateExt, JobState> WS_STATE2STATE;

	static {
		Map<JobStateExt, JobState> map = new HashMap<JobStateExt, JobState>();
		map.put(JobStateExt.CANCELED, JobState.Canceled);
		map.put(JobStateExt.CONFIGURING, JobState.Configuring);
		map.put(JobStateExt.FAILED, JobState.Failed);
		map.put(JobStateExt.FINISHED, JobState.Finished);
		map.put(JobStateExt.QUEUED, JobState.Queued);
		map.put(JobStateExt.RUNNING, JobState.Running);
		map.put(JobStateExt.SUBMITTED, JobState.Submitted);
		WS_STATE2STATE = Collections.unmodifiableMap(map);
	}

	private String sessionID;

	private UserAndLimitationManagementWsSoap userAndLimitationManagement;

	private JobManagementWsSoap jobManagement;

	private FileTransferWsSoap fileTransfer;

	private final String projectId;

	private final Map<Long, P_FileTransferPool> filetransferPoolMap = new HashMap<>();

	private final HaaSClientSettings settings;

	

	public HaaSClient(HaaSClientSettings settings) {
		this.settings = settings;
		this.projectId = settings.getProjectId();
	}

	public long createJob(JobSettings settings, Collection<Entry<String, String>> templateParameters) {
		try {
			return doCreateJob(settings, templateParameters);
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

	public HaaSFileTransfer startFileTransfer(long jobId) {
		return startFileTransfer(jobId, DUMMY_TRANSFER_FILE_PROGRESS);
	}

	public TunnelToNode openTunnel(long jobId, String nodeIP, int localPort, int remotePort) {
		MidlewareTunnel tunnel;
		try {
			tunnel = new MidlewareTunnel(Executors.newCachedThreadPool(), jobId, nodeIP, getSessionID());
			tunnel.open(localPort, remotePort);
			return new TunnelToNode() {
				@Override
				public void close() throws IOException {
					tunnel.close();
				}

				@Override
				public int getLocalPort() {
					return tunnel.getLocalPort();
				}

				@Override
				public String getLocalHost() {
					return tunnel.getLocalHost();
				}
			};
		} catch (ServiceException | IOException e) {
			log.error(e.getMessage(), e);
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

			final Collection<Long> tasksId = info.getTasks().getSubmittedTaskInfoExt().stream().map(ti -> ti.getId())
					.collect(Collectors.toList());
			return new JobInfo() {
				private List<String> ips;

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
					return toGregorian(info.getStartTime());
				};

				@Override
				public java.util.Calendar getEndTime() {
					return toGregorian(info.getEndTime());
				};

				@Override
				public Calendar getCreationTime() {
					return toGregorian(info.getCreationTime());
				};

				@Override
				public List<String> getNodesIPs() {
					if (ips == null) {
						try {
							ips = getJobManagement().getAllocatedNodesIPs(jobId, getSessionID()).getString().stream()
									.collect(Collectors.toList());
						} catch (RemoteException | ServiceException e) {
							log.error(e.getMessage(), e);
						}
					}
					return ips;
				}
			};
		} catch (RemoteException | ServiceException e) {
			throw new HaaSClientException(e);
		}

	}

	public Collection<JobFileContentExt> downloadPartsOfJobFiles(Long jobId, HaaSClient.SynchronizableFiles files) {
		try {
			ArrayOfTaskFileOffsetExt fileOffsetExt = new ArrayOfTaskFileOffsetExt();
			fileOffsetExt.getTaskFileOffsetExt().addAll(files.getFiles());
			return getFileTransfer().downloadPartsOfJobFilesFromCluster(jobId, fileOffsetExt, getSessionID())
					.getJobFileContentExt();
		} catch (RemoteException | ServiceException e) {
			throw new HaaSClientException(e);
		}
	}

	public Collection<String> getChangedFiles(long jobId) {
		try {
			return getFileTransfer().listChangedFilesForJob(jobId, getSessionID()).getString();
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

	public void deleteJob(long id) {
		try {
			getJobManagement().deleteJob(id, getSessionID());
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

	private long doCreateJob(JobSettings jobSettings, Collection<Entry<String, String>> templateParameters)
			throws RemoteException, ServiceException {
		Collection<TaskSpecificationExt> taskSpec = Arrays
				.asList(createTaskSpecification(jobSettings, templateParameters));
		JobSpecificationExt jobSpecification = createJobSpecification(jobSettings, taskSpec);
		SubmittedJobInfoExt job = getJobManagement().createJob(jobSpecification, getSessionID());
		return job.getId();
	}

	private ScpClient getScpClient(FileTransferMethodExt fileTransfer)
			throws UnsupportedEncodingException, JSchException {
		byte[] pvtKey = fileTransfer.getCredentials().getPrivateKey().getBytes("UTF-8");
		return new ScpClient(fileTransfer.getServerHostname(), fileTransfer.getCredentials().getUsername(), pvtKey);
	}

	private JobSpecificationExt createJobSpecification(JobSettings jobSettings,
			Collection<TaskSpecificationExt> tasks) {
		JobSpecificationExt testJob = new JobSpecificationExt();
		testJob.setName(jobSettings.getJobName());
		testJob.setMinCores(jobSettings.getNumberOfCoresPerNode() * jobSettings.getNumberOfNodes());
		testJob.setMaxCores(jobSettings.getNumberOfCoresPerNode() * jobSettings.getNumberOfNodes());
		testJob.setPriority(JobPriorityExt.AVERAGE);
		testJob.setProject(projectId);
		testJob.setWaitingLimit(null);
		testJob.setWalltimeLimit(jobSettings.getWalltimeLimit());
		testJob.setNotificationEmail(settings.getEmail());
		testJob.setPhoneNumber(settings.getPhone());
		testJob.setNotifyOnAbort(false);
		testJob.setNotifyOnFinish(false);
		testJob.setNotifyOnStart(false);
		testJob.setClusterNodeTypeId(jobSettings.getClusterNodeType());
		testJob.setEnvironmentVariables(new ArrayOfEnvironmentVariableExt());
		testJob.setTasks(getAndFill(new ArrayOfTaskSpecificationExt(), a -> a.getTaskSpecificationExt().addAll(tasks)));
		return testJob;
	}

	private TaskSpecificationExt createTaskSpecification(JobSettings jobSettings,
			Collection<Entry<String, String>> templateParameters) {

		TaskSpecificationExt testTask = new TaskSpecificationExt();
		testTask.setName(jobSettings.getJobName() + "-task");
		testTask.setMinCores(jobSettings.getNumberOfCoresPerNode() * jobSettings.getNumberOfNodes());
		testTask.setMaxCores(jobSettings.getNumberOfCoresPerNode() * jobSettings.getNumberOfNodes());
		testTask.setWalltimeLimit(jobSettings.getWalltimeLimit());
		testTask.setRequiredNodes(null);
		testTask.setIsExclusive(false);
		testTask.setIsRerunnable(false);
		testTask.setStandardInputFile(null);
		testTask.setStandardOutputFile("console_Stdout");
		testTask.setStandardErrorFile("console_Stderr");
		testTask.setProgressFile("console_Stdprog");
		testTask.setLogFile("console_Stdlog");
		testTask.setClusterTaskSubdirectory(null);
		testTask.setCommandTemplateId(jobSettings.getTemplateId());
		testTask.setEnvironmentVariables(new ArrayOfEnvironmentVariableExt());
		testTask.setDependsOn(null);
		testTask.setTemplateParameterValues(getAndFill(new ArrayOfCommandTemplateParameterValueExt(),
				t -> t.getCommandTemplateParameterValueExt()
						.addAll(templateParameters.stream()
								.map(pair -> createCommandTemplateParameterValueExt(pair.getKey(), pair.getValue()))
								.collect(Collectors.toList()))));
		return testTask;
	}

	private String authenticate() throws RemoteException, ServiceException {
		return getUserAndLimitationManagement()
				.authenticateUserPassword(createPasswordCredentialsExt(settings.getUserName(), settings.getPassword()));
	}

	private UserAndLimitationManagementWsSoap getUserAndLimitationManagement() throws ServiceException {
		if (userAndLimitationManagement == null) {
			userAndLimitationManagement = new UserAndLimitationManagementWs().getUserAndLimitationManagementWsSoap12();
		}
		return userAndLimitationManagement;
	}

	private JobManagementWsSoap getJobManagement() throws ServiceException {
		if (jobManagement == null) {
			jobManagement = new JobManagementWs().getJobManagementWsSoap12();
		}
		return jobManagement;
	}

	private FileTransferWsSoap getFileTransfer() throws ServiceException {
		if (fileTransfer == null) {
			fileTransfer = new FileTransferWs().getFileTransferWsSoap12();
		}
		return fileTransfer;
	}

	String getSessionID() throws RemoteException, ServiceException {
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

	private static <T> T getAndFill(T value, Consumer<T> filler) {
		filler.accept(value);
		return value;
	}

	private static Calendar toGregorian(XMLGregorianCalendar time) {
		return Optional.ofNullable(time).map(t -> t.toGregorianCalendar()).orElse(null);
	}

	private static CommandTemplateParameterValueExt createCommandTemplateParameterValueExt(String key, String value) {
		CommandTemplateParameterValueExt result = new CommandTemplateParameterValueExt();
		result.setCommandParameterIdentifier(key);
		result.setParameterValue(value);
		return result;
	}

	private static PasswordCredentialsExt createPasswordCredentialsExt(String userName, String password) {
		PasswordCredentialsExt result = new PasswordCredentialsExt();
		result.setUsername(userName);
		result.setPassword(password);
		return result;
	}

}
