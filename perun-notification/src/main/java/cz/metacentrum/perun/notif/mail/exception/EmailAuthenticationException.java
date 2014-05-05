package cz.metacentrum.perun.notif.mail.exception;

/**
 * Exception thrown on failed authentication.
 */
public class EmailAuthenticationException extends EmailException {

	private static final long serialVersionUID = -3518304048940117166L;

	/**
	 * Constructor for EmailAuthenticationException.
	 *
	 * @param msg message
	 */
	public EmailAuthenticationException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for EmailAuthenticationException.
	 *
	 * @param msg the detail message
	 * @param cause the root cause from the mail API in use
	 */
	public EmailAuthenticationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructor for EmailAuthenticationException.
	 *
	 * @param cause the root cause from the mail API in use
	 */
	public EmailAuthenticationException(Throwable cause) {
		super("Authentication failed", cause);
	}
}
