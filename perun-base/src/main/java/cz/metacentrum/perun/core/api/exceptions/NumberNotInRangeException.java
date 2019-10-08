package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when a number is not in the range of numbers.
 *
 * @author Michal Šťava
 */
public class NumberNotInRangeException extends InternalErrorException {
	static final long serialVersionUID = 0;


	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public NumberNotInRangeException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public NumberNotInRangeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public NumberNotInRangeException(Throwable cause) {
		super(cause);
	}
}
