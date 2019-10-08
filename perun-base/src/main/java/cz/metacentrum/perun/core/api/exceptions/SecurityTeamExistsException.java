package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the securityTeam exists in the database
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class SecurityTeamExistsException extends EntityExistsException {

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public SecurityTeamExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SecurityTeamExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SecurityTeamExistsException(Throwable cause) {
		super(cause);
	}
}
