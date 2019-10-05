package cz.metacentrum.perun.core.api.exceptions;

/**
 * Thrown when trying to remove an userExtSource which has already been removed
 *
 * @author Michal Stava
 */
public class UserExtSourceAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public UserExtSourceAlreadyRemovedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public UserExtSourceAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public UserExtSourceAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
