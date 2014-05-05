package cz.metacentrum.perun.notif.mail.exception;

/**
 * Exception thrown if illegal message properties are encountered.
 */
public class EmailParseException extends EmailException {

	private static final long serialVersionUID = 5097798456468717059L;

	/**
	 * Constructor for EmailParseException.
	 *
	 * @param msg the detail message
	 */
	public EmailParseException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for EmailParseException.
	 *
	 * @param msg the detail message
	 * @param cause the root cause from the mail API in use
	 */
	public EmailParseException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructor for EmailParseException.
	 *
	 * @param cause the root cause from the mail API in use
	 */
	public EmailParseException(Throwable cause) {
		super("Could not parse mail", cause);
	}

}
