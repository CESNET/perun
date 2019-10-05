package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the login wasn't found in the specific namespace
 *
 * @author Michal Prochazka
 */
public class LoginNotExistsException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public LoginNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public LoginNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public LoginNotExistsException(Throwable cause) {
		super(cause);
	}
}
