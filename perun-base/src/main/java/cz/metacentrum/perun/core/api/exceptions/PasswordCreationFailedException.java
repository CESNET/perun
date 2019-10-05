package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the password could not be changed because the login is unavailable or other various problems occurred
 *
 * @author Michal Prochazka
 */
public class PasswordCreationFailedException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public PasswordCreationFailedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PasswordCreationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PasswordCreationFailedException(Throwable cause) {
		super(cause);
	}
}
