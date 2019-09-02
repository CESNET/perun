package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the ExtSource has already been deleted or removed from the Vo/group.
 *
 * @author Michal Stava
 */
public class ExtSourceAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ExtSourceAlreadyRemovedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtSourceAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtSourceAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
