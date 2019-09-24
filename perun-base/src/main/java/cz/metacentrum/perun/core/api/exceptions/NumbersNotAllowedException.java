package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when in the name of entity is any number.
 *
 * @author Michal Šťava
 */
public class NumbersNotAllowedException extends InternalErrorException {
	static final long serialVersionUID = 0;


	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public NumbersNotAllowedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public NumbersNotAllowedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public NumbersNotAllowedException(Throwable cause) {
		super(cause);
	}
}
