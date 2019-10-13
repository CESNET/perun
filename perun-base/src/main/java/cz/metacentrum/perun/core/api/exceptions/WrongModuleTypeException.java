package cz.metacentrum.perun.core.api.exceptions;


/**
 * Raised when working with a module which doesn't have expected type.
 *
 * @author Slavek Licehammer
 */
public class WrongModuleTypeException extends InternalErrorException {
	static final long serialVersionUID = 0;


	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public WrongModuleTypeException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongModuleTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongModuleTypeException(Throwable cause) {
		super(cause);
	}
}
