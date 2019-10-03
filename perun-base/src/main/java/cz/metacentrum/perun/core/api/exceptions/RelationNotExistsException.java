package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception means that some relation or dependency not exists which prevents to execute the action.
 *
 * @author Michal Stava
 */
public class RelationNotExistsException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public RelationNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public RelationNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public RelationNotExistsException(Throwable cause) {
		super(cause);
	}
}
