package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the password could not be changed because it failed for various reasons
 *
 * @author Michal Prochazka
 */
public class PasswordChangeFailedException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public PasswordChangeFailedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PasswordChangeFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PasswordChangeFailedException(Throwable cause) {
		super(cause);
	}
}
