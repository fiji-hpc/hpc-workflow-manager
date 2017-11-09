package cz.it4i.fiji.haas_java_client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.rpc.ServiceException;

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

	private interface Constants {
		String USER_NAME = "testuser";
		String PASSWORD = "testpass";
		String EMAIL = "jan.kozusznik@vsb.cz";
		String PHONE = "999111000";
	}

	private String sessionID;

	private Path workDirectory;

	private UserAndLimitationManagementWsSoap userAndLimitationManagement;

	private JobManagementWsSoap jobManagement;

	private FileTransferWsSoap fileTransfer;

	private Integer timeOut;

	private Long templateId;

	private Long clusterNodeType;

	private String projectId;

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

	public HaaSClient(Path workDirectory, Long templateId, Integer timeOut,Long  clusterNodeType, String projectId) {
		super();
		this.workDirectory = workDirectory;
		this.templateId = templateId;
		this.timeOut = timeOut;
		this.clusterNodeType = clusterNodeType;
		this.projectId = projectId;
	}

	public long start(Iterable<Path> files, String name, Collection<Entry<String, String>> templateParameters) {

		TaskSpecificationExt taskSpec = createTaskSpecification(name, templateId, templateParameters);
		JobSpecificationExt jobSpecification = createJobSpecification(name, Arrays.asList(taskSpec));
		try {
			SubmittedJobInfoExt job = getJobManagement().createJob(jobSpecification, getSessionID());
			System.out.printf("Created job: %d\n", job.getId());
			FileTransferMethodExt fileTransfer = getFileTransfer().getFileTransferMethod(job.getId(), getSessionID());

			try (ScpClient scpClient = getScpClient(fileTransfer)) {

				for (Path file : files) {
					System.out.println("Uploading file: " + file.getFileName());
					scpClient.upload(file, fileTransfer.getSharedBasepath() + "//" + file.getFileName());
					System.out.println("File uploaded.");
				}
			}
			getFileTransfer().endFileTransfer(job.getId(), fileTransfer, getSessionID());
			// submit job
			job = getJobManagement().submitJob((long) job.getId(), getSessionID());
			System.out.printf("Submitted job ID: %d\n", job.getId());
			return job.getId();
		} catch (ServiceException | JSchException | IOException e) {
			throw new RuntimeException(e);
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

	public void download(long jobId) {
		try {
			FileTransferMethodExt ft = getFileTransfer().getFileTransferMethod(jobId, getSessionID());
			try (ScpClient scpClient = getScpClient(ft)) {

				for (String fileName : getFileTransfer().listChangedFilesForJob(jobId, getSessionID())) {
					fileName = fileName.replaceAll("/", "");
					Path rFile = workDirectory.resolve(fileName);
					scpClient.download(ft.getSharedBasepath() + "//" + fileName, rFile);
				}
			}
			getFileTransfer().endFileTransfer(jobId, ft, getSessionID());
		} catch (IOException | JSchException | ServiceException e) {
			throw new HaaSClientException(e);
		}
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
		testJob.setNotificationEmail(Constants.EMAIL);
		testJob.setPhoneNumber(Constants.PHONE);
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
				.authenticateUserPassword(new PasswordCredentialsExt(Constants.USER_NAME, Constants.PASSWORD));
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
			jobManagement = new JobManagementWsLocator().getJobManagementWsSoap12();
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
}
