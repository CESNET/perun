package cz.metacentrum.perun.core.api.exceptions;

/**
 * Exception is thrown when the destination does not exist
 *
 * @author Michal Prochazka
 */
public class DestinationNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public DestinationNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public DestinationNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public DestinationNotExistsException(Throwable cause) {
		super(cause);
	}
}
