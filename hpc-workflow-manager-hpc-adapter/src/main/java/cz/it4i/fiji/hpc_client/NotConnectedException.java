package cz.it4i.fiji.hpc_client;



public class NotConnectedException extends HPCClientException {

	private static final long serialVersionUID = 2800021329326219636L;

	public NotConnectedException() {
		super();
	}

	public NotConnectedException(String message, Throwable cause,
		boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NotConnectedException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotConnectedException(String message) {
		super(message);
	}

	public NotConnectedException(Throwable cause) {
		super(cause);
	}

}
