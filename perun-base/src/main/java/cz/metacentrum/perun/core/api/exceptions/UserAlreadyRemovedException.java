package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the user has already been removed or when the user
 * has not been found in the blacklist of the security team
 *
 * @author Michal Stava
 */
public class UserAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public UserAlreadyRemovedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public UserAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public UserAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
