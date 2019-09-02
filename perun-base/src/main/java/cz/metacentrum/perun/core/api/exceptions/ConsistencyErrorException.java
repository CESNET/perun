package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when some inconsistency in underlaying data sources occur.
 *
 * @author Slavek Licehammer
 */
public class ConsistencyErrorException extends InternalErrorException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ConsistencyErrorException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ConsistencyErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ConsistencyErrorException(Throwable cause) {
		super(cause);
	}
}
