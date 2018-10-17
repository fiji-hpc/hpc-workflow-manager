
package cz.it4i.fiji.haas_java_client;

import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.List;

import javax.xml.rpc.ServiceException;

import cz.it4i.fiji.scpclient.AuthFailException;
import cz.it4i.fiji.scpclient.TransferFileProgress;

public class HaasFileTransferReconnectingAfterAuthFail implements HaaSFileTransfer {

	private static final int MAX_ATTEMPTS_FOR_RECONNECTION = 5;

	public interface Supplier <T> {
		T get() throws ServiceException, RemoteException;
	}
	
	private Supplier<HaaSFileTransfer> haasFileTransferFactory;
	private HaaSFileTransfer haasFileTransfer;
	private Runnable reconnectCommand;

	public HaasFileTransferReconnectingAfterAuthFail(
		Supplier<HaaSFileTransfer> haasFileTransferFactory,
		Runnable reconnectCommand) throws RemoteException, ServiceException
	{
		super();
		this.haasFileTransferFactory = haasFileTransferFactory;
		this.haasFileTransfer = haasFileTransferFactory.get();
		this.reconnectCommand = reconnectCommand;
	}

	@Override
	public void close() {
		this.haasFileTransfer.close();
	}

	@Override
	public void upload(UploadingFile file) throws InterruptedIOException {
		doWithRepeatedReconnect(() -> {
			haasFileTransfer.upload(file);
			return null;
		});
	}

	@Override
	public void download(String files, Path workDirectory)
		throws InterruptedIOException
	{
		doWithRepeatedReconnect(() -> {
			haasFileTransfer.download(files, workDirectory);
			return null;
		});
	}

	@Override
	public List<Long> obtainSize(List<String> files)
		throws InterruptedIOException
	{
		return doWithRepeatedReconnect(() -> haasFileTransfer.obtainSize(files));
	}

	@Override
	public List<String> getContent(List<String> logs) {
		try {
			return doWithRepeatedReconnect(() -> haasFileTransfer.getContent(logs));
		}
		catch (InterruptedIOException exc) {
			throw new RuntimeException(exc);
		}
	}

	@Override
	public void setProgress(TransferFileProgress progress) {
		try {
			doWithRepeatedReconnect(() -> {
				haasFileTransfer.setProgress(progress);
				return null;
			});
		}
		catch (InterruptedIOException exc) {
			throw new RuntimeException(exc);
		}
	}

	private interface CallableInteruptable<T> {

		T call() throws InterruptedIOException;
	}

	private <T> T doWithRepeatedReconnect(CallableInteruptable<T> runnable)
		throws InterruptedIOException
	{
		int attempts = 0;
		do {
			try {
				return runnable.call();
			}
			catch (HaaSClientException e) {
				if (e.getCause() instanceof AuthFailException) {
					if (attempts <= MAX_ATTEMPTS_FOR_RECONNECTION) {
						attempts++;
						reconnect();
						continue;
					}
				}
				throw e;
			}
		}
		while (true);
	}

	private void reconnect() {
		haasFileTransfer.close();
		reconnectCommand.run();
		try {
			haasFileTransfer = haasFileTransferFactory.get();
		}
		catch (RemoteException | ServiceException exc) {
			throw new HaaSClientException(exc);
		}
	}
}
