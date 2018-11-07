package cz.it4i.fiji.scpclient;

import java.io.IOException;
import java.io.InputStream;

public class AckowledgementChecker {
	
	private int lastStatus;
	
	private StringBuilder lastMessage;
	
	public boolean checkAck(InputStream in) throws IOException {
		lastMessage = new StringBuilder();
		return checkAck(in, lastMessage);
	}
	
	
	public String getLastMessage() {
		return lastMessage.toString();
	}
	
	
	public int getLastStatus() {
		return lastStatus;
	}
	
	private boolean checkAck(InputStream in, StringBuilder sb) throws IOException {
		lastStatus = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (lastStatus == 0) return true;
		if (lastStatus == -1) return false;

		if (lastStatus == 1 || lastStatus == 2) {
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			}
			while (c != '\n');
		}
		return lastStatus == 0;
	}
}
