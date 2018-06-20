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

public class MidlewareTunnel implements Closeable {

	public static final Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_java_client.MidlewareTunnel.class);
	
	private static final int TIMEOUT = 1000;

	private static final int DEFAULT_BACKLOG = 50;
	private final long jobId;
	private ServerSocket ss;
	private final DataTransferWsSoap dataTransfer;
	private Future<?> mainFuture;
	private CountDownLatch mainLatch;
	private final String ipAddress;
	private final String sessionCode;
	private final ExecutorService executorService;

	

	
	public MidlewareTunnel(ExecutorService executorService,long jobId, String hostIp, String sessionCode) {
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
	
	public void open(int localport, int port) throws UnknownHostException, IOException {
		open(localport, port, DEFAULT_BACKLOG);
	}
	
	public void open(int localport, int port,int backlog) throws UnknownHostException, IOException {
		if (ss != null) {
			throw new IllegalStateException();
		}
		ss = new ServerSocket(localport, backlog, InetAddress.getByName("localhost"));
		ss.setSoTimeout(TIMEOUT);
		mainLatch = new CountDownLatch(1);
		mainFuture = executorService.submit(() -> {
			try {
				while (!Thread.interrupted() && !ss.isClosed()) {
					try (Socket soc = ss.accept()) {
						doTransfer(soc, port);
					} catch (SocketTimeoutException e) {
						// ignore and check interruption
					} catch (IOException e) {
						log.error(e.getMessage(), e);
						break;
					}
				}
			} finally {
				log.info("MiddlewareTunnel - interrupted");
				mainLatch.countDown();
		
			}
		});
	}

	@Override
	public void close() throws IOException {
		mainFuture.cancel(true);
		try {
			mainLatch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error(e.getMessage(), e);
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

	private void doTransfer(Socket soc, int port) {
		log.info("START: doTransfer");
		DataTransferMethodExt transfer = dataTransfer.getDataTransferMethod(ipAddress, port, jobId, sessionCode);
		P_Connection connection = new P_Connection(soc);
		connection.setClientHandler(c -> {
			sendToMiddleware(c);
			log.info("endDataTransfer");
			dataTransfer.endDataTransfer(transfer, sessionCode);
			log.info("endDataTransfer - DONE");

		});
		connection.setServerHandler(c -> readFromMiddleware(c));
		connection.establish();
		
		log.info("END: doTransfer");
	}

	private void sendToMiddleware(P_Connection connection) {
		log.info("START: sendToMiddleware");
		try {
			InputStream is = Channels.newInputStream(Channels.newChannel(connection.getSocket().getInputStream()));
			int len;
			byte[] buffer = new byte[4096];
			while (!connection.isServerClosed() && -1 != (len = is.read(buffer))) {
				if(connection.isServerClosed()) {
					break;
				}
				byte[] sending;
				if (len == 0) {
					continue;
				}
				if (buffer.length != len) {
					sending = new byte[len];
					System.arraycopy(buffer, 0, sending, 0, len);
				} else {
					sending = buffer;
				}

				int result = dataTransfer.sendDataToJobNode(sending, jobId, ipAddress, sessionCode);
				if (result <= 0) {
					return;
				}
			}
			connection.clientClosed();

		} catch (InterruptedIOException e) {
			return;
		} catch (SocketException e) {
			if (!e.getMessage().equals("Socket closed")) {
				log.error(e.getMessage(), e);
			}
		} catch (IOException e) {
			return;
		} finally {
			log.info("END: sendToMiddleware");
		}
	}

	private void readFromMiddleware(P_Connection connection) {
		log.info("START: readFromMiddleware");
		try {
			OutputStream os = connection.getSocket().getOutputStream();
			byte[] received = null;
			while (!connection.isClientClosed()
					&& null != (received = dataTransfer.readDataFromJobNode(jobId, ipAddress, sessionCode))) {
				if(connection.isClientClosed()) {
					break;
				}
				if (received.length > 0) {
					os.write(received);
					os.flush();
				}
			}
			os.flush();
			connection.serverClosed();
		} catch (InterruptedIOException e) {
			return;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return;
		} finally {
			log.info("END: readFromMiddleware");
		}
	}

	private class P_Connection {
		private static final int FROM_CLIENT = 0;
		private static final int FROM_SERVER = 1;

		private final Socket socket;

		private final Runnable[] runnable = new Runnable [2];

		private final Future<?>[] futures = new Future[2];

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

		public void clientClosed() {
			setClosed(FROM_CLIENT);
		}
		
		public void serverClosed() {
			setClosed(FROM_SERVER);
		}
		
		public boolean isClientClosed() {
			return isClosed(FROM_CLIENT);
		}
		
		public boolean isServerClosed() {
			return isClosed(FROM_SERVER);
		}
		
		public void establish() {
			CountDownLatch localLatch = new CountDownLatch(2);
			for (int i = 0; i < runnable.length; i++) {
				int final_i = i;
				futures[i] = executorService.submit(() -> {
					runnable[final_i].run();
					localLatch.countDown();
					return null;
				});
			}
			
			try {
				localLatch.await();
			} catch (InterruptedException e) {
				stop(localLatch);
				Thread.currentThread().interrupt();
			};
			
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

		private boolean isClosed(int type) {
			return Thread.interrupted();
		}

		private void setClosed(int type) {
			futures[(type + 1) % 2].cancel(true);
		}

	}
}