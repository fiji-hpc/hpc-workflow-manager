package cz.it4i.fiji.hpc_client;

public class AuthenticationException extends HPCClientException {

	private static final long serialVersionUID = 7883710844778853196L;

	public AuthenticationException() {
	}

	public AuthenticationException(String message) {
		super(message);
	}

	public AuthenticationException(Throwable cause) {
		super(cause);
	}

	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthenticationException(String message, Throwable cause,
		boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
