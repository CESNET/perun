package cz.metacentrum.perun.core.api.exceptions;

/**
 * Thrown when trying to create a VO with an id or a name of another existing VO
 *
 * @author Martin Kuba
 */
public class VoExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public VoExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public VoExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public VoExistsException(Throwable cause) {
		super(cause);
	}
}
