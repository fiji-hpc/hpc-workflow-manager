
package cz.it4i.fiji.haas_java_client;

import com.jcraft.jsch.JSchException;

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

import cz.it4i.fiji.haas_java_client.proxy.ArrayOfCommandTemplateParameterValueExt;
import cz.it4i.fiji.haas_java_client.proxy.ArrayOfEnvironmentVariableExt;
import cz.it4i.fiji.haas_java_client.proxy.ArrayOfTaskFileOffsetExt;
import cz.it4i.fiji.haas_java_client.proxy.ArrayOfTaskSpecificationExt;
import cz.it4i.fiji.haas_java_client.proxy.CommandTemplateParameterValueExt;
import cz.it4i.fiji.haas_java_client.proxy.DataTransferMethodExt;
import cz.it4i.fiji.haas_java_client.proxy.DataTransferWs;
import cz.it4i.fiji.haas_java_client.proxy.DataTransferWsSoap;
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

	public static final TransferFileProgress DUMMY_TRANSFER_FILE_PROGRESS =
		new TransferFileProgress()
		{

			@Override
			public void dataTransfered(final long bytesTransfered) {}
		};

	public static ProgressNotifier DUMMY_PROGRESS_NOTIFIER =
		new ProgressNotifier()
		{

			@Override
			public void setTitle(final String title) {}

			@Override
			public void setItemCount(final int count, final int total) {}

			@Override
			public void setCount(final int count, final int total) {}

			@Override
			public void itemDone(final Object item) {}

			@Override
			public void done() {}

			@Override
			public void addItem(final Object item) {}
		};

	public static UploadingFile getUploadingFile(final Path file) {
		return new UploadingFile() {

			@Override
			public InputStream getInputStream() {
				try {
					return Files.newInputStream(file);
				}
				catch (final IOException e) {
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
				}
				catch (final IOException e) {
					log.error(e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}

			@Override
			public long getLastTime() {
				try {
					return Files.getLastModifiedTime(file).toMillis();
				}
				catch (final IOException e) {
					log.error(e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}

		};
	}

	static public class SynchronizableFiles {

		private final Collection<TaskFileOffsetExt> files = new LinkedList<>();

		public void addFile(final long taskId, final SynchronizableFileType type,
			final long offset)
		{
			final TaskFileOffsetExt off = new TaskFileOffsetExt();
			off.setFileType(getType(type));
			off.setSubmittedTaskInfoId(taskId);
			off.setOffset(offset);
			files.add(off);
		}

		private Collection<TaskFileOffsetExt> getFiles() {
			return files;
		}

		private SynchronizableFilesExt getType(final SynchronizableFileType type) {
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

	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_java_client.HaaSClient.class);

	final static private Map<JobStateExt, JobState> WS_STATE2STATE;

	static {
		final Map<JobStateExt, JobState> map = new HashMap<>();
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

	private FileTransferWsSoap fileTransferWS;

	private final String projectId;

	private final Map<Long, P_FileTransferPool> filetransferPoolMap =
		new HashMap<>();

	private final HaaSClientSettings settings;

	private DataTransferWsSoap dataTransferWs;

	public HaaSClient(final HaaSClientSettings settings) {
		this.settings = settings;
		this.projectId = settings.getProjectId();
	}

	public long createJob(final JobSettings jobSettings,
		final Collection<Entry<String, String>> templateParameters)
	{
		return doCreateJob(jobSettings, templateParameters);
	}

	public HaaSFileTransfer startFileTransfer(final long jobId,
		final TransferFileProgress notifier)
	{
		try {
			return createFileTransfer(jobId, notifier);
		}
		catch (RemoteException | ServiceException | UnsupportedEncodingException
				| JSchException e)
		{
			throw new HaaSClientException(e);
		}
	}

	public HaaSFileTransfer startFileTransfer(final long jobId) {
		return startFileTransfer(jobId, DUMMY_TRANSFER_FILE_PROGRESS);
	}

	public TunnelToNode openTunnel(final long jobId, final String nodeIP,
		final int localPort, final int remotePort)
	{
		MiddlewareTunnel tunnel;
		try {
			tunnel = new MiddlewareTunnel(Executors.newCachedThreadPool(), jobId,
				nodeIP, getSessionID());
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
		}
		catch (final IOException e) {
			log.error(e.getMessage(), e);
			throw new HaaSClientException(e);
		}
	}

	public void submitJob(final long jobId) {
		doSubmitJob(jobId);
	}

	public JobInfo obtainJobInfo(final long jobId) {
		final SubmittedJobInfoExt info = getJobManagement().getCurrentInfoForJob(
			jobId, getSessionID());

		final Collection<Long> tasksId = info.getTasks().getSubmittedTaskInfoExt()
			.stream().map(ti -> ti.getId()).collect(Collectors.toList());
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
			}

			@Override
			public java.util.Calendar getEndTime() {
				return toGregorian(info.getEndTime());
			}

			@Override
			public Calendar getCreationTime() {
				return toGregorian(info.getCreationTime());
			}

			@Override
			public List<String> getNodesIPs() {
				if (ips == null) {
					ips = getJobManagement().getAllocatedNodesIPs(jobId, getSessionID())
						.getString().stream().collect(Collectors.toList());

				}
				return ips;
			}
		};
	}

	public Collection<JobFileContentExt> downloadPartsOfJobFiles(final Long jobId,
		final HaaSClient.SynchronizableFiles files)
	{
		final ArrayOfTaskFileOffsetExt fileOffsetExt =
			new ArrayOfTaskFileOffsetExt();
		fileOffsetExt.getTaskFileOffsetExt().addAll(files.getFiles());
		return getFileTransfer().downloadPartsOfJobFilesFromCluster(jobId,
			fileOffsetExt, getSessionID()).getJobFileContentExt();
	}

	public Collection<String> getChangedFiles(final long jobId) {
		return getFileTransfer().listChangedFilesForJob(jobId, getSessionID())
			.getString();
	}

	public void cancelJob(final Long jobId) {
		getJobManagement().cancelJob(jobId, getSessionID());
	}

	public void deleteJob(final long id) {
		getJobManagement().deleteJob(id, getSessionID());
	}

	public HaaSDataTransfer startDataTransfer(final long jobId,
		final int nodeNumber, final int port)
	{
		return createDataTransfer(jobId, nodeNumber, port);
	}

	synchronized String getSessionID() {
		if (sessionID == null) {
			sessionID = authenticate();
		}
		return sessionID;
	}

	private HaaSFileTransferImp createFileTransfer(final long jobId,
		final TransferFileProgress progress) throws RemoteException,
		UnsupportedEncodingException, ServiceException, JSchException
	{
		final P_FileTransferPool pool = filetransferPoolMap.computeIfAbsent(jobId,
			id -> new P_FileTransferPool(id));
		final FileTransferMethodExt ft = pool.obtain();
		try {
			return new HaaSFileTransferImp(ft, getScpClient(ft), progress) {

				@Override
				public void close() {
					super.close();
					try {
						pool.release();
					}
					catch (RemoteException | ServiceException e) {
						throw new HaaSClientException(e);
					}
				}
			};
		}
		catch (UnsupportedEncodingException | JSchException e) {
			pool.release();
			throw e;
		}
	}

	private HaaSDataTransfer createDataTransfer(final long jobId,
		final int nodeNumber, final int port)
	{
		final String host = getJobManagement().getAllocatedNodesIPs(jobId,
			getSessionID()).getString().get(nodeNumber);
		final DataTransferWsSoap ws = getDataTransfer();
		final DataTransferMethodExt dataTransferMethodExt = ws
			.getDataTransferMethod(host, port, jobId, getSessionID());
		final String sessionId = getSessionID();
		return new HaaSDataTransfer() {

			@Override
			public void close() throws IOException {
				if (log.isDebugEnabled()) {
					log.debug("close");
				}
				ws.endDataTransfer(dataTransferMethodExt, sessionId);
				if (log.isDebugEnabled()) {
					log.debug("close - DONE");
				}
			}

			@Override
			public void write(final byte[] buffer) {
				if (log.isDebugEnabled()) {
					log.debug("write: {}", new String(buffer));
				}
				ws.writeDataToJobNode(buffer, jobId, host, sessionId, false);
				if (log.isDebugEnabled()) {
					log.debug("write - DONE");
				}
			}

			@Override
			public byte[] read() {
				if (log.isDebugEnabled()) {
					log.debug("read: ");
				}
				final byte[] result = ws.readDataFromJobNode(jobId, host, sessionId);
				if (log.isDebugEnabled()) {
					log.debug("read - DONE: \"{}\"", result != null ? new String(result)
						: "EOF");
				}
				return result;
			}

			@Override
			public void closeConnection() {
				if (log.isDebugEnabled()) {
					log.debug("closeConnection");
				}
				ws.writeDataToJobNode(null, jobId, host, sessionId, true);
				if (log.isDebugEnabled()) {
					log.debug("closeConnection - DONE");
				}
			}
		};
	}

	private void doSubmitJob(final long jobId) {
		getJobManagement().submitJob(jobId, getSessionID());
	}

	private long doCreateJob(final JobSettings jobSettings,
		final Collection<Entry<String, String>> templateParameters)
	{
		final Collection<TaskSpecificationExt> taskSpec = Arrays.asList(
			createTaskSpecification(jobSettings, templateParameters));
		final JobSpecificationExt jobSpecification = createJobSpecification(
			jobSettings, taskSpec);
		final SubmittedJobInfoExt job = getJobManagement().createJob(
			jobSpecification, getSessionID());
		return job.getId();
	}

	private ScpClient getScpClient(final FileTransferMethodExt fileTransfer)
		throws UnsupportedEncodingException, JSchException
	{
		final byte[] pvtKey = fileTransfer.getCredentials().getPrivateKey()
			.getBytes("UTF-8");
		return new ScpClient(fileTransfer.getServerHostname(), fileTransfer
			.getCredentials().getUsername(), pvtKey);
	}

	private JobSpecificationExt createJobSpecification(
		final JobSettings jobSettings, final Collection<TaskSpecificationExt> tasks)
	{
		final JobSpecificationExt testJob = new JobSpecificationExt();
		testJob.setName(jobSettings.getJobName());
		testJob.setMinCores(jobSettings.getNumberOfCoresPerNode() * jobSettings
			.getNumberOfNodes());
		testJob.setMaxCores(jobSettings.getNumberOfCoresPerNode() * jobSettings
			.getNumberOfNodes());
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
		testJob.setTasks(getAndFill(new ArrayOfTaskSpecificationExt(), a -> a
			.getTaskSpecificationExt().addAll(tasks)));
		return testJob;
	}

	private TaskSpecificationExt createTaskSpecification(
		final JobSettings jobSettings,
		final Collection<Entry<String, String>> templateParameters)
	{

		final TaskSpecificationExt testTask = new TaskSpecificationExt();
		testTask.setName(jobSettings.getJobName() + "-task");
		testTask.setMinCores(jobSettings.getNumberOfCoresPerNode() * jobSettings
			.getNumberOfNodes());
		testTask.setMaxCores(jobSettings.getNumberOfCoresPerNode() * jobSettings
			.getNumberOfNodes());
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
		testTask.setTemplateParameterValues(getAndFill(
			new ArrayOfCommandTemplateParameterValueExt(), t -> t
				.getCommandTemplateParameterValueExt().addAll(templateParameters
					.stream().map(pair -> createCommandTemplateParameterValueExt(pair
						.getKey(), pair.getValue())).collect(Collectors.toList()))));
		return testTask;
	}

	@SuppressWarnings("restriction")
	private String authenticate() {
		try {
		return getUserAndLimitationManagement().authenticateUserPassword(
			createPasswordCredentialsExt(settings.getUserName(), settings
				.getPassword()));
		}
		catch (com.sun.xml.internal.ws.client.ClientTransportException e) {
			if(e.getMessage().contains("The server sent HTTP status code 500: Internal Server Error")) {
				throw new AuthenticationException(e);
			}
			throw e;
		}
	}

	synchronized private DataTransferWsSoap getDataTransfer() {
		if (dataTransferWs == null) {
			dataTransferWs = new DataTransferWs().getDataTransferWsSoap12();
		}
		return dataTransferWs;
	}

	synchronized private UserAndLimitationManagementWsSoap
		getUserAndLimitationManagement()
	{
		if (userAndLimitationManagement == null) {
			userAndLimitationManagement = new UserAndLimitationManagementWs()
				.getUserAndLimitationManagementWsSoap12();
		}
		return userAndLimitationManagement;
	}

	synchronized private JobManagementWsSoap getJobManagement() {
		if (jobManagement == null) {
			jobManagement = new JobManagementWs().getJobManagementWsSoap12();
		}
		return jobManagement;
	}

	synchronized private FileTransferWsSoap getFileTransfer() {
		if (fileTransferWS == null) {
			fileTransferWS = new FileTransferWs().getFileTransferWsSoap12();
		}
		return fileTransferWS;
	}

	public static class P_ProgressNotifierDecorator4Size extends
		P_ProgressNotifierDecorator
	{

		private static final int SIZE_RATIO = 20;

		public P_ProgressNotifierDecorator4Size(final ProgressNotifier notifier) {
			super(notifier);

		}

		@Override
		public void setItemCount(final int count, final int total) {
			super.setItemCount(count, total);
			setCount(count, total * SIZE_RATIO);
		}
	}

	public static class P_ProgressNotifierDecorator implements ProgressNotifier {

		private final ProgressNotifier notifier;

		public P_ProgressNotifierDecorator(final ProgressNotifier notifier) {
			this.notifier = notifier;
		}

		@Override
		public void setTitle(final String title) {
			notifier.setTitle(title);
		}

		@Override
		public void setCount(final int count, final int total) {
			notifier.setCount(count, total);
		}

		@Override
		public void addItem(final Object item) {
			notifier.addItem(item);
		}

		@Override
		public void setItemCount(final int count, final int total) {
			notifier.setItemCount(count, total);
		}

		@Override
		public void itemDone(final Object item) {
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

		public P_FileTransferPool(final long jobId) {
			this.factory = () -> getFileTransfer().getFileTransferMethod(jobId,
				getSessionID());
			this.destroyer = val -> getFileTransfer().endFileTransfer(jobId, val,
				sessionID);
		}

		public synchronized FileTransferMethodExt obtain() throws RemoteException,
			ServiceException
		{
			if (holded == null) {
				holded = factory.get();
			}
			counter++;
			return holded;
		}

		public synchronized void release() throws RemoteException,
			ServiceException
		{
			if (--counter == 0) {
				destroyer.accept(holded);
				holded = null;
			}
		}

	}

	private static <T> T getAndFill(final T value, final Consumer<T> filler) {
		filler.accept(value);
		return value;
	}

	private static Calendar toGregorian(final XMLGregorianCalendar time) {
		return Optional.ofNullable(time).map(t -> t.toGregorianCalendar()).orElse(
			null);
	}

	private static CommandTemplateParameterValueExt
		createCommandTemplateParameterValueExt(final String key, final String value)
	{
		final CommandTemplateParameterValueExt result =
			new CommandTemplateParameterValueExt();
		result.setCommandParameterIdentifier(key);
		result.setParameterValue(value);
		return result;
	}

	private static PasswordCredentialsExt createPasswordCredentialsExt(
		final String userName, final String password)
	{
		final PasswordCredentialsExt result = new PasswordCredentialsExt();
		result.setUsername(userName);
		result.setPassword(password);
		return result;
	}

}
