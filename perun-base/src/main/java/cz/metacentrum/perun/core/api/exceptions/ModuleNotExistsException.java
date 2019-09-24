package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception is thrown when the virtual attribute module for an attribute does not exist
 *
 * @author Slavek Licehammer
 */
public class ModuleNotExistsException extends InternalErrorException {
	static final long serialVersionUID = 0;


	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ModuleNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ModuleNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ModuleNotExistsException(Throwable cause) {
		super(cause);
	}
}
