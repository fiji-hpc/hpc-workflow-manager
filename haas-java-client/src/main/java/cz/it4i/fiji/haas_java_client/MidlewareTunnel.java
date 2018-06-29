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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas_java_client.proxy.DataTransferMethodExt;
import cz.it4i.fiji.haas_java_client.proxy.DataTransferWs;
import cz.it4i.fiji.haas_java_client.proxy.DataTransferWsSoap;

class MidlewareTunnel implements Closeable {

	public static final Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_java_client.MidlewareTunnel.class);

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

	public MidlewareTunnel(ExecutorService executorService, long jobId, String hostIp, String sessionCode) {
		this.jobId = jobId;
		this.dataTransfer = new DataTransferWs().getDataTransferWsSoap12();
		((BindingProvider) dataTransfer).getRequestContext().put("javax.xml.ws.client.connectionTimeout", "" + TIMEOUT);
		((BindingProvider) dataTransfer).getRequestContext().put("javax.xml.ws.client.receiveTimeout", "" + TIMEOUT);
		this.ipAddress = hostIp;
		this.sessionCode = sessionCode;
		this.executorService = executorService;
	}

	public void open(int port) throws UnknownHostException, IOException {
		open(0, port);
	}

	public void open(int localport, int port) throws IOException {
		open(localport, port, DEFAULT_BACKLOG);
	}

	public void open(int localport, int port, int backlog) throws UnknownHostException, IOException {
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
						doTransfer(soc, port);
						if (log.isDebugEnabled()) {
							log.debug("endDataTransfer");
						}
						
						if (log.isDebugEnabled()) {
							log.debug("endDataTransfer - DONE");
						}
					} catch (SocketTimeoutException e) {
						// ignore and check interruption
					} catch (IOException e) {
						log.error(e.getMessage(), e);
						break;
					}
				}
				if(lastConnection != null) {
					lastConnection.finishIfNeeded();
				}
			} finally {
				if (log.isDebugEnabled()) {
					log.debug("MiddlewareTunnel - interrupted - socket is closed: " + ss.isClosed());
				}

				mainLatch.countDown();
			}
		});
	}

	@Override
	synchronized public void close() throws IOException {
		if(ss == null) {
			return;
		}
		mainFuture.cancel(true);
		try {
			mainLatch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error(e.getMessage(), e);
		}
		if(dataTransferMethod != null) {
			dataTransfer.endDataTransfer(dataTransferMethod, sessionCode);
			dataTransferMethod = null;
		}
		if (ss != null) {
			ss.close();
			ss = null;
		}
		executorService.shutdown();
	}

	public int getLocalPort() {
		return ss.getLocalPort();
	}

	public String getLocalHost() {
		return ss.getInetAddress().getHostAddress();
	}

	synchronized private void obtainTransferMethodIfNeeded(int port) {
		if(dataTransferMethod == null) {
			dataTransferMethod = dataTransfer.getDataTransferMethod(ipAddress, port, jobId,
					sessionCode);
		}
	}

	private void doTransfer(Socket soc, int port) {
		log.debug("START: doTransfer");
		if(lastConnection != null) {
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

	private void sendToMiddleware(P_Connection connection) {
		if (log.isDebugEnabled()) {
			log.debug("START: sendToMiddleware");
		}
		try {
			InputStream is = Channels.newInputStream(Channels.newChannel(connection.getSocket().getInputStream()));
			int len;
			byte[] buffer = new byte[sendBufferData];
			try {
				while (-1 != (len = is.read(buffer))) {
	
					if (len == 0) {
						continue;
					}
					if (!sendToMiddleware(buffer, len)) {
						break;
					}
					if (log.isDebugEnabled()) {
						log.debug("send " + len + " bytes to middleware");
						log.debug("send data: " + new String(buffer, 0, Math.min(len, 100)));
	
					}
				}
			} finally {
				sendEOF2Middleware();
			}
		} catch (InterruptedIOException e) {
			log.error(e.getMessage(), e);
		} catch (SocketException e) {
			if (!e.getMessage().equals("Socket closed")) {
				log.error(e.getMessage(), e);
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return;
		} catch(RuntimeException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("END: sendToMiddleware");
			}
		}
	}

	private boolean sendToMiddleware(byte[] buffer, int len) {
		byte[] sending;
		int toSend = len;
		int offset = 0;
		int zeroCounter = 0;
		while (toSend != 0) {
			if (toSend != buffer.length || offset != 0) {
				sending = new byte[toSend];
				System.arraycopy(buffer, offset, sending, 0, toSend);
			} else {
				sending = buffer;
			}
			int reallySend = dataTransfer.writeDataToJobNode(sending, jobId, ipAddress, sessionCode, false);
			if (reallySend == -1) {
				return false;
			}
			toSend -= reallySend;
			offset += reallySend;
			if (reallySend == 0) {
				zeroCounter++;
				if (zeroCounter >= ZERO_COUNT_THRESHOLD) {
					if (log.isDebugEnabled()) {
						log.debug("zero bytes sent to middleware for " + zeroCounter + " time");
					}
					return false;
				}
				try {
					Thread.sleep(ZERO_COUNT_PAUSE);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return false;
				}
			} else {
				zeroCounter = 0;
			}
		}
		;
		return true;
	}

	private void sendEOF2Middleware() {
		if (log.isDebugEnabled()) {
			log.debug("sendEOF to middleware");
		}
		dataTransfer.writeDataToJobNode(null, jobId, ipAddress, sessionCode, true);
	}

	private void readFromMiddleware(P_Connection connection) {
		if (log.isDebugEnabled()) {
			log.debug("START: readFromMiddleware");
		}
		try (OutputStream os = connection.getSocket().getOutputStream()) {
			
			byte[] received = null;
			int zeroCounter = 0;
			while (null != (received = dataTransfer.readDataFromJobNode(jobId, ipAddress, sessionCode))) {
				if (received.length > 0) {
					os.write(received);
					os.flush();
					zeroCounter = 0;
				} else {
					zeroCounter++;
					if (zeroCounter >= ZERO_COUNT_THRESHOLD) {
						if (log.isDebugEnabled()) {
							log.debug("zero bytes received from middleware for " + zeroCounter + " time");
						}
						break;
					}
					try {
						Thread.sleep(ZERO_COUNT_PAUSE);
					} catch (InterruptedException e) {
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
		} catch (InterruptedIOException e) {
			return;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return;
		} finally {
			log.debug("END: readFromMiddleware");
		}
	}

	private class P_Connection {
		private static final int FROM_CLIENT = 0;
		private static final int FROM_SERVER = 1;

		private final Socket socket;

		private final Runnable[] runnable = new Runnable[2];

		
		private final CompletableFuture<?>[] futures = new CompletableFuture[2];
		
		private final CountDownLatch latchFromClient = new CountDownLatch(1);
		
		private final CountDownLatch latchOfBothDirections = new CountDownLatch(2);

		public P_Connection(Socket soc) {
			this.socket = soc;
		}

		public void setClientHandler(Consumer<P_Connection> callable) {
			runnable[FROM_CLIENT] = () -> callable.accept(this);
		}

		public void setServerHandler(Consumer<P_Connection> callable) {
			runnable[FROM_SERVER] = () -> callable.accept(this);
		}

		public Socket getSocket() {
			return socket;
		}

		public void establish() {
			
			for (int i = 0; i < runnable.length; i++) {
				int final_i = i;
				futures[i] = CompletableFuture.runAsync(() -> {
					runnable[final_i].run();
				}, executorService).whenComplete((id,e) -> {
					if (final_i == FROM_CLIENT) {
						latchFromClient.countDown();
					}

					latchOfBothDirections.countDown();
					if(e != null) {
						log.error(e.getMessage(), e);
					}
				});
			}

			try {
				latchFromClient.await();
			} catch (InterruptedException e) {
				stop(latchOfBothDirections);
				Thread.currentThread().interrupt();
			}
		}
		
		public void finishIfNeeded() {
			stop(latchOfBothDirections);
		}

		private void stop(CountDownLatch localLatch) {
			for (Future<?> thread : futures) {
				thread.cancel(true);
			}
			try {
				localLatch.await();
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}

		
	}
}