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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

public class HaaSClient {

	public static List<Long> getSizes(List<String> asList, ScpClient scpClient, ProgressNotifier notifier)
			throws JSchException, IOException {
		List<Long> result = new LinkedList<>();

		String item;
		notifier.addItem(item = "Checking sizes");
		for (String lfile : asList) {
			result.add(scpClient.size(lfile));
			notifier.setItemCount(result.size(), asList.size());
		}
		notifier.itemDone(item);
		return result;
	}

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

	public interface UploadingFile {
		InputStream getInputStream();

		String getName();

		long getLength();

		long getLastTime();
	}

	static public class SynchronizableFiles {

		private Collection<TaskFileOffsetExt> files = new LinkedList<>();

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

	private String sessionID;

	private UserAndLimitationManagementWsSoap userAndLimitationManagement;

	private JobManagementWsSoap jobManagement;

	private FileTransferWsSoap fileTransfer;

	private Integer timeOut;

	private Long templateId;

	private Long clusterNodeType;

	private String projectId;

	public static ProgressNotifier DUMMY_NOTIFIER = new ProgressNotifier() {

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

	private Settings settings;

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

	public HaaSClient(Settings settings) {
		this.settings = settings;
		this.templateId = settings.getTemplateId();
		this.timeOut = settings.getTimeout();
		this.clusterNodeType = settings.getClusterNodeType();
		this.projectId = settings.getProjectId();
	}

	public long start(Iterable<Path> files, String name, Collection<Entry<String, String>> templateParameters) {
		Iterable<UploadingFile> uploadingFiles = StreamSupport.stream(files.spliterator(), false)
				.map(HaaSClient::getUploadingFile).collect(Collectors.toList());
		return start(uploadingFiles, name, templateParameters, DUMMY_NOTIFIER);
	}

	public long start(Iterable<UploadingFile> files, String name, Collection<Entry<String, String>> templateParameters,
			ProgressNotifier notifier) {
		notifier.setTitle("Starting job");
		try {
			long jobId = doCreateJob(name, templateParameters);
			try (HaaSFileTransfer transfer = startFileTransfer(jobId, notifier)) {
				transfer.upload(files);
			}
			doSubmitJob(jobId);
			return jobId;
		} catch (ServiceException | IOException e) {
			throw new HaaSClientException(e);
		}

	}

	public long createJob(String name, Collection<Entry<String, String>> templateParameters) {
		try {
			return doCreateJob(name, templateParameters);
		} catch (RemoteException | ServiceException e) {
			throw new RuntimeException(e);
		}
	}

	public HaaSFileTransfer startFileTransfer(long jobId, ProgressNotifier notifier) {
		try {
			FileTransferMethodExt ft = getFileTransfer().getFileTransferMethod(jobId, getSessionID());
			return new HaaSFileTransferImp(ft, getSessionID(), jobId, getFileTransfer(), getScpClient(ft), notifier);
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

				public java.util.Calendar getStartTime() {
					return info.getStartTime();
				};

				public java.util.Calendar getEndTime() {
					return info.getEndTime();
				};

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

	private void doSubmitJob(long jobId) throws RemoteException, ServiceException {
		getJobManagement().submitJob(jobId, getSessionID());
	}

	private long doCreateJob(String name, Collection<Entry<String, String>> templateParameters)
			throws RemoteException, ServiceException {
		TaskSpecificationExt taskSpec = createTaskSpecification(name, templateId, templateParameters);
		JobSpecificationExt jobSpecification = createJobSpecification(name, Arrays.asList(taskSpec));
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
		private ProgressNotifier notifier;

		public P_ProgressNotifierDecorator(ProgressNotifier notifier) {
			this.notifier = notifier;
		}

		public void setTitle(String title) {
			notifier.setTitle(title);
		}

		public void setCount(int count, int total) {
			notifier.setCount(count, total);
		}

		public void addItem(Object item) {
			notifier.addItem(item);
		}

		public void setItemCount(int count, int total) {
			notifier.setItemCount(count, total);
		}

		public void itemDone(Object item) {
			notifier.itemDone(item);
		}

		public void done() {
			notifier.done();
		}
	}

}
