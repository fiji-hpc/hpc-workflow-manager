package cz.it4i.fiji.hpc_client;

public class HPCClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public HPCClientException() {
	}

	public HPCClientException(String message) {
		super(message);
	}

	public HPCClientException(Throwable cause) {
		super(cause);
	}

	public HPCClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public HPCClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
