package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception means that some relation or dependency exists which prevents to execute the action.
 *
 * @author Slavek Licehammer
 */
public class RelationExistsException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public RelationExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public RelationExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public RelationExistsException(Throwable cause) {
		super(cause);
	}
}
