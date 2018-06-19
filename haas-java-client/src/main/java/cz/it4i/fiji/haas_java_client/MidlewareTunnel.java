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

import javax.xml.ws.BindingProvider;

import cz.it4i.fiji.haas_java_client.proxy.DataTransferMethodExt;
import cz.it4i.fiji.haas_java_client.proxy.DataTransferWs;
import cz.it4i.fiji.haas_java_client.proxy.DataTransferWsSoap;

public class MidlewareTunnel implements Closeable {

	private static final int TIMEOUT = 1000;
	private final long jobId;
	private ServerSocket ss;
	private final DataTransferWsSoap dataTransfer;
	private Thread thread;
	private final String ipAddress;
	private final String sessionCode;
	

	public MidlewareTunnel(long jobId, String hostIp, String sessionCode) {
		this.jobId = jobId;
		this.dataTransfer = new DataTransferWs().getDataTransferWsSoap12();
		((BindingProvider) dataTransfer).getRequestContext().put("javax.xml.ws.client.connectionTimeout", "" + TIMEOUT);
		((BindingProvider) dataTransfer).getRequestContext().put("javax.xml.ws.client.receiveTimeout", "" + TIMEOUT);
		this.ipAddress = hostIp;
		this.sessionCode = sessionCode;
	}

	public void open(int port) throws UnknownHostException, IOException {
		ss = new ServerSocket(0, 50, InetAddress.getByName("localhost"));
		ss.setSoTimeout(TIMEOUT);
		
		thread = new Thread () {
			

			@Override
			public void run() {
				while(!Thread.interrupted() && !ss.isClosed()) {
					try(Socket soc = ss.accept()) {
						doTransfer(soc);
					} catch (SocketTimeoutException e) {
						//ignore and check interruption
					 } catch (IOException e) {
						TestCommunicationWithNodes.log.error(e.getMessage(), e);
						break;
					 }
				}
				
			}

			private void doTransfer(Socket soc) {
				TestCommunicationWithNodes.log.info("START: doTransfer");
				Thread helpingThread = new Thread() {
					@Override
					public void run() {
						readFromMiddleware(soc);
					}
				};
				DataTransferMethodExt transfer = dataTransfer.getDataTransferMethod(ipAddress, port, jobId,
						sessionCode);
				helpingThread.start();
				sendToMiddleware(soc);
				TestCommunicationWithNodes.log.info("endDataTransfer");
				dataTransfer.endDataTransfer(transfer, sessionCode);
				TestCommunicationWithNodes.log.info("endDataTransfer - DONE");
				
				helpingThread.interrupt();
				try {
					helpingThread.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				
				TestCommunicationWithNodes.log.info("END: doTransfer");
			}
		};
		thread.start();
	}

	@Override
	public void close() throws IOException {
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			TestCommunicationWithNodes.log.error(e.getMessage(), e);
		}
		if(ss != null) {
			ss.close();
			ss = null;
		}
	}

	public int getLocalPort() {
		return ss.getLocalPort();
	}

	private void sendToMiddleware(Socket soc) {
		TestCommunicationWithNodes.log.info("START: sendToMiddleware");
		StringBuilder sb = new StringBuilder();
		try {
			InputStream is = Channels.newInputStream(Channels.newChannel(soc.getInputStream()));
			int len;
			byte []buffer = new byte[4096];
			while(!Thread.interrupted() && -1 != (len = is.read(buffer))) {
				byte[] sending;
				if(len == 0) {
					continue;
				}
				if(buffer.length != len) {
					sending = new byte[len];
					System.arraycopy(buffer, 0, sending, 0, len);
				} else {
					sending = buffer;
				}
				
				sb.append(new String(sending));
				int result = dataTransfer.sendDataToJobNode(sending, jobId, ipAddress, sessionCode);
				if(result <= 0) {
					return;
				}
			}
		} catch(InterruptedIOException e) {
			return;
		} catch(SocketException e) {
			if(!e.getMessage().equals("Socket closed")) {
				TestCommunicationWithNodes.log.error(e.getMessage(), e);
			}
		} catch (IOException e) {
			return;
		} finally {
			TestCommunicationWithNodes.log.info("sent: <" + sb + ">");
			TestCommunicationWithNodes.log.info("END: sendToMiddleware");
		}
	}

			private void readFromMiddleware(Socket soc) {
		TestCommunicationWithNodes.log.info("START: readFromMiddleware");
		StringBuilder sb = new StringBuilder();
		try {
			OutputStream os = soc.getOutputStream();
			byte [] received = null;
			while(!Thread.interrupted() && null != (received = dataTransfer.readDataFromJobNode(jobId, ipAddress, sessionCode))) {
				if(received.length > 0) {
					//logData("received",received);
					os.write(received);
					os.flush();
					sb.append(new String(received));
				}
				
			}
			os.flush();
		} catch(InterruptedIOException e) {
			return;
		} catch (IOException e) {
			TestCommunicationWithNodes.log.error(e.getMessage(), e);
			return;
		} finally {
			TestCommunicationWithNodes.log.info("END: readFromMiddleware");
		}
	}
}