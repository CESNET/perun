package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when verifying Multi-factor authentication fails.
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class MFAuthenticationException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 *
	 * @param message message with details about the cause
	 */
	public MFAuthenticationException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 *
	 * @param message message with details about the cause
	 * @param cause   Throwable that caused throwing of this exception
	 */
	public MFAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 *
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MFAuthenticationException(Throwable cause) {
		super(cause);
	}
}
