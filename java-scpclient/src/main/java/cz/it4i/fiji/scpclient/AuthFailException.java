package cz.it4i.fiji.scpclient;

import com.jcraft.jsch.JSchException;


public class AuthFailException extends JSchException {

	public AuthFailException() {
	}

	public AuthFailException(String s) {
		super(s);
	}

	public AuthFailException(String s, Throwable e) {
		super(s, e);
	}

}
