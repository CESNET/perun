package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when name of entity is not long enough.
 *
 * @author Michal Šťava
 */
public class MinSizeExceededException extends InternalErrorException {
	static final long serialVersionUID = 0;


	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public MinSizeExceededException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MinSizeExceededException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public MinSizeExceededException(Throwable cause) {
		super(cause);
	}
}
