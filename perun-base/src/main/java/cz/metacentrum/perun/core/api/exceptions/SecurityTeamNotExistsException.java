package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the securityTeam was not found in the database
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class SecurityTeamNotExistsException extends EntityNotExistsException {

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public SecurityTeamNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SecurityTeamNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SecurityTeamNotExistsException(Throwable cause) {
		super(cause);
	}
}
