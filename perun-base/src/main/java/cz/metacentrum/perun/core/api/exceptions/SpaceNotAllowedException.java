package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when the name of entity contains at least one space.
 *
 * @author Michal Šťava
 */
public class SpaceNotAllowedException extends InternalErrorException {
	static final long serialVersionUID = 0;


	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public SpaceNotAllowedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SpaceNotAllowedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SpaceNotAllowedException(Throwable cause) {
		super(cause);
	}
}
