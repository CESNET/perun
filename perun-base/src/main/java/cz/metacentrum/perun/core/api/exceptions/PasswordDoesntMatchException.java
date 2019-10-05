package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the old password doesn't match
 *
 * @author Michal Prochazka
 */
public class PasswordDoesntMatchException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public PasswordDoesntMatchException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PasswordDoesntMatchException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PasswordDoesntMatchException(Throwable cause) {
		super(cause);
	}
}
