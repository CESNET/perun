package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the resource can't be removed because it is not in the database anymore
 *
 * @author Michal Stava
 */
public class ResourceAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ResourceAlreadyRemovedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ResourceAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ResourceAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
