package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when some property of entity is too long.
 *
 * @author Michal Šťava
 */
public class MaxSizeExceededException extends InternalErrorException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public MaxSizeExceededException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MaxSizeExceededException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MaxSizeExceededException(Throwable cause) {
		super(cause);
	}
}
