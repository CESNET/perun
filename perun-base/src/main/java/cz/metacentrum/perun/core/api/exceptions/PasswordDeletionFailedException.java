package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the password could not be deleted because it's not reserved, it's in use or other various reasons
 *
 * @author Michal Prochazka
 */
public class PasswordDeletionFailedException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public PasswordDeletionFailedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PasswordDeletionFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PasswordDeletionFailedException(Throwable cause) {
		super(cause);
	}
}
