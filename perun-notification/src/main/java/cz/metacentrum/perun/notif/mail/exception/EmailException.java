package cz.metacentrum.perun.notif.mail.exception;

/**
 * Abstract class for Mail exception.
 *
 * @author tomas.tunkl
 *
 */
public abstract class EmailException extends RuntimeException {

	private static final long serialVersionUID = -1815506118987744701L;

	/**
	 * Constructor for EmailException.
	 *
	 * @param msg the detail message
	 */
	public EmailException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for EmailException.
	 *
	 * @param msg the detail message
	 * @param cause the root cause from the mail API in use
	 */
	public EmailException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
