package cz.it4i.fiji.haas_java_client;

public class HaaSClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public HaaSClientException() {
	}

	public HaaSClientException(String message) {
		super(message);
	}

	public HaaSClientException(Throwable cause) {
		super(cause);
	}

	public HaaSClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public HaaSClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
