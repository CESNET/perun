package cz.metacentrum.perun.core.api.exceptions;

/**
 * Thrown when the given login is already blocked for the given namespace
 * Thrown also if the given namespace is null and the given login is already blocked globally
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class LoginIsAlreadyBlockedException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public LoginIsAlreadyBlockedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public LoginIsAlreadyBlockedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public LoginIsAlreadyBlockedException(Throwable cause) {
		super(cause);
	}
}
