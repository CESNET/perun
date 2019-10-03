package cz.metacentrum.perun.core.api.exceptions;

/**
 * Security team not exists in perun system yet
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
