
package cz.it4i.fiji.haas_java_client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.proxy.DataTransferMethodExt;
import cz.it4i.fiji.haas_java_client.proxy.DataTransferWs;
import cz.it4i.fiji.haas_java_client.proxy.DataTransferWsSoap;

class MiddlewareTunnel implements Closeable {

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_java_client.MiddlewareTunnel.class);

	private static final int TIMEOUT = 1000;

	private static final int DEFAULT_BACKLOG = 50;

	private static final int ZERO_COUNT_THRESHOLD = 10;

	private static final long ZERO_COUNT_PAUSE = 500;

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024 * 32;

	private final long jobId;

	private ServerSocket ss;

	private final DataTransferWsSoap dataTransfer;

	private Future<?> mainFuture;

	private CountDownLatch mainLatch;

	private final String ipAddress;

	private final String sessionCode;

	private final ExecutorService executorService;

	private final int sendBufferData = DEFAULT_BUFFER_SIZE;

	private P_Connection lastConnection;

	private DataTransferMethodExt dataTransferMethod;

	public MiddlewareTunnel(final ExecutorService executorService,
		final long jobId, final String hostIp, final String sessionCode)
	{
		this.jobId = jobId;
		this.dataTransfer = new DataTransferWs().getDataTransferWsSoap12();
		((BindingProvider) dataTransfer).getRequestContext().put(
			"javax.xml.ws.client.connectionTimeout", "" + TIMEOUT);
		((BindingProvider) dataTransfer).getRequestContext().put(
			"javax.xml.ws.client.receiveTimeout", "" + TIMEOUT);
		this.ipAddress = hostIp;
		this.sessionCode = sessionCode;
		this.executorService = executorService;
	}

	public void open(final int port) throws UnknownHostException, IOException {
		open(0, port);
	}

	public void open(final int localport, final int port) throws IOException {
		open(localport, port, DEFAULT_BACKLOG);
	}

	public void open(final int localport, final int port, final int backlog)
		throws UnknownHostException, IOException
	{
		if (ss != null) {
			throw new IllegalStateException();
		}
		ss = new ServerSocket(localport, backlog, InetAddress.getLoopbackAddress());
		ss.setSoTimeout(TIMEOUT);
		mainLatch = new CountDownLatch(1);
		mainFuture = executorService.submit(() -> {
			try {
				while (!Thread.interrupted() && !ss.isClosed()) {
					try (Socket soc = ss.accept()) {
						obtainTransferMethodIfNeeded(port);
						doTransfer(soc);
					}
					catch (final SocketTimeoutException e) {
						// ignore and check interruption
					}
					catch (final IOException e) {
						log.error(e.getMessage(), e);
						break;
					}
				}
				if (lastConnection != null) {
					lastConnection.finishIfNeeded();
				}
				if (log.isDebugEnabled()) {
					log.debug("MiddlewareTunnel - interrupted - socket is closed: " + ss
						.isClosed());
				}

			}
			catch (final RuntimeException e) {
				log.error(e.getMessage(), e);
				throw e;
			}
			finally {
				mainLatch.countDown();
			}
		});
	}

	public int getLocalPort() {
		return ss.getLocalPort();
	}

	public String getLocalHost() {
		return ss.getInetAddress().getHostAddress();
	}

	@Override
	synchronized public void close() throws IOException {
		if (ss == null) {
			return;
		}
		mainFuture.cancel(true);
		try {
			mainLatch.await();
		}
		catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error(e.getMessage(), e);
		}
		if (dataTransferMethod != null) {
			if (log.isDebugEnabled()) {
				log.debug("endDataTransfer");
			}
			dataTransfer.endDataTransfer(dataTransferMethod, sessionCode);
			if (log.isDebugEnabled()) {
				log.debug("endDataTransfer - DONE");
			}
			dataTransferMethod = null;
		}
		if (ss != null) {
			ss.close();
			ss = null;
		}
		executorService.shutdown();
	}

	synchronized private void obtainTransferMethodIfNeeded(final int port) {
		if (dataTransferMethod == null) {
			dataTransferMethod = dataTransfer.getDataTransferMethod(ipAddress, port,
				jobId, sessionCode);
		}
	}

	private void doTransfer(final Socket soc) {
		log.debug("START: doTransfer");
		if (lastConnection != null) {
			lastConnection.finishIfNeeded();
		}
		lastConnection = new P_Connection(soc);
		lastConnection.setClientHandler(c -> sendToMiddleware(c));
		lastConnection.setServerHandler(c -> readFromMiddleware(c));
		lastConnection.establish();
		if (log.isDebugEnabled()) {
			log.debug("END: doTransfer");
		}
	}

	private void sendToMiddleware(final P_Connection connection) {
		if (log.isDebugEnabled()) {
			log.debug("START: sendToMiddleware");
		}
		try {
			final InputStream is = Channels.newInputStream(Channels.newChannel(
				connection.getInputStream()));
			int len;
			final byte[] buffer = new byte[sendBufferData];
			try {
				while (-1 != (len = is.read(buffer))) {
					if (len == 0) {
						continue;
					}
					try {
						if (!sendToMiddleware(buffer, len)) {
							break;
						}
					}
					finally {
						connection.dataSentNotify();
					}
					if (log.isDebugEnabled()) {
						log.debug("send " + len + " bytes to middleware");
						log.debug("send data: " + new String(buffer, 0, Math.min(len,
							100)));

					}
				}
				if (log.isDebugEnabled()) {
					log.debug("EOF detected from client");
				}
			}
			finally {
				sendEOF2Middleware();
			}
		}
		catch (final InterruptedIOException e) {
			log.error(e.getMessage(), e);
		}
		catch (final SocketException e) {
			if (!e.getMessage().equals("Socket closed") || !e.getMessage().equals(
				"Connection reset"))
			{
				log.error(e.getMessage(), e);
			}
		}
		catch (final IOException e) {
			log.error(e.getMessage(), e);
			return;
		}
		catch (final RuntimeException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		finally {
			if (log.isDebugEnabled()) {
				log.debug("END: sendToMiddleware");
			}
		}
	}

	private boolean sendToMiddleware(final byte[] buffer, final int len) {
		byte[] sending;
		int toSend = len;
		int offset = 0;
		int zeroCounter = 0;
		while (toSend != 0) {
			if (toSend != buffer.length || offset != 0) {
				sending = new byte[toSend];
				System.arraycopy(buffer, offset, sending, 0, toSend);
			}
			else {
				sending = buffer;
			}
			if (log.isDebugEnabled()) {
				log.debug("writing to middleware");
			}
			final int reallySend = dataTransfer.writeDataToJobNode(sending, jobId,
				ipAddress, sessionCode, false);

			if (reallySend == -1) {
				return false;
			}
			toSend -= reallySend;
			offset += reallySend;
			if (reallySend == 0) {
				zeroCounter++;
				if (zeroCounter >= ZERO_COUNT_THRESHOLD) {
					if (log.isDebugEnabled()) {
						log.debug("zero bytes sent to middleware for " + zeroCounter +
							" time");
					}
					return false;
				}
				try {
					Thread.sleep(ZERO_COUNT_PAUSE);
				}
				catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
					return false;
				}
			}
			else {
				zeroCounter = 0;
			}
		}
		return true;
	}

	private void sendEOF2Middleware() {
		if (log.isDebugEnabled()) {
			log.debug("sendEOF to middleware");
		}
		try {
			dataTransfer.writeDataToJobNode(null, jobId, ipAddress, sessionCode, true);
		} 
		catch (WebServiceException e) {
			//ignore this
		}
	}

	private void readFromMiddleware(final P_Connection connection) {
		try {
			connection.waitForFirstDataSent();
		}
		catch (InterruptedException exc1) {
			return;
		}
		if (log.isDebugEnabled()) {
			log.debug("START: readFromMiddleware");
		}
		try {
			final OutputStream os = connection.getOutputStream();
			byte[] received = null;
			int zeroCounter = 0;
			while (null != (received = dataTransfer.readDataFromJobNode(jobId,
				ipAddress, sessionCode)))
			{
				if (log.isDebugEnabled()) {
					log.debug("receiving from middleware");
				}
				if (received.length > 0) {
					os.write(received);
					os.flush();
					zeroCounter = 0;
				}
				else {
					zeroCounter++;
					if (zeroCounter >= ZERO_COUNT_THRESHOLD) {
						if (log.isDebugEnabled()) {
							log.debug("zero bytes received from middleware for " +
								zeroCounter + " time");
						}
						break;
					}
					try {
						Thread.sleep(ZERO_COUNT_PAUSE);
					}
					catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}
				if (log.isDebugEnabled()) {
					log.debug("received " + received.length + " bytes from middleware");
					if (received.length > 0) {
						log.debug("received data " + new String(received));
					}
				}
			}
			if (received == null) {
				if (log.isDebugEnabled()) {
					log.debug("EOF from middleware detected");
				}
			}
		}
		catch (final InterruptedIOException e) {
			// ignore this
		}
		catch (SocketException e) {
			if (!e.getMessage().equals("Broken pipe (Write failed)")) {
				log.error(e.getMessage(), e);
			}
		}
		catch (final IOException e) {
			log.error(e.getMessage(), e);
		}
		finally {
			try {
				connection.shutdownOutput();
			}
			catch (final IOException exc) {
				log.error(exc.getMessage(), exc);
			}
			log.debug("END: readFromMiddleware");
		}
	}

	private class P_Connection {

		private static final int FROM_CLIENT = 0;
		private static final int FROM_SERVER = 1;

		private final Socket socket;

		private final Runnable[] runnable = new Runnable[2];

		private final Thread[] threads = new Thread[2];

		private final CountDownLatch latchOfBothDirections = new CountDownLatch(2);

		private final CountDownLatch dataSentFlag = new CountDownLatch(1);

		public P_Connection(final Socket soc) {
			this.socket = soc;
		}

		public void shutdownOutput() throws IOException {
			socket.shutdownOutput();
		}

		public InputStream getInputStream() throws IOException {
			return socket.getInputStream();
		}

		public OutputStream getOutputStream() throws IOException {
			return socket.getOutputStream();
		}

		public void setClientHandler(final Consumer<P_Connection> callable) {
			runnable[FROM_CLIENT] = () -> callable.accept(this);
		}

		public void setServerHandler(final Consumer<P_Connection> callable) {
			runnable[FROM_SERVER] = () -> callable.accept(this);
		}

		public void establish() {

			for (int i = 0; i < runnable.length; i++) {
				final int final_i = i;
				CompletableFuture.runAsync(() -> {
					threads[final_i] = Thread.currentThread();
					runnable[final_i].run();
				}, executorService).whenComplete((id, e) -> {
					latchOfBothDirections.countDown();
					if (e != null) {
						log.error(e.getMessage(), e);
					}
				});
			}

			try {
				latchOfBothDirections.await();
			}
			catch (final InterruptedException e) {
				stop(latchOfBothDirections);
				Thread.currentThread().interrupt();
			}
		}

		public void finishIfNeeded() {
			stop(latchOfBothDirections);
		}

		public void dataSentNotify() {
			if (dataSentFlag.getCount() > 0) {
				dataSentFlag.countDown();
			}
		}

		public void waitForFirstDataSent() throws InterruptedException {
			dataSentFlag.await();
		}

		private void stop(final CountDownLatch localLatch) {
			for (final Thread thread : threads) {
				if (thread != null) {
					thread.interrupt();
				}
			}
			try {
				localLatch.await();
			}
			catch (final InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}

	}
}
