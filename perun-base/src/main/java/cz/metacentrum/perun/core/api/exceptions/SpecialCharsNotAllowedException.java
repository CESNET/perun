package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when in the name of entity is any special char.
 *
 * @author Michal Šťava
 */
public class SpecialCharsNotAllowedException extends InternalErrorException {
	static final long serialVersionUID = 0;


	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public SpecialCharsNotAllowedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SpecialCharsNotAllowedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SpecialCharsNotAllowedException(Throwable cause) {
		super(cause);
	}
}
