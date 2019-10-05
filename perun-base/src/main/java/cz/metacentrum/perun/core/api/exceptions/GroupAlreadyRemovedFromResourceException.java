package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when trying to remove the group from the resource that has already been removed
 *
 * @author Michal Stava
 */
public class GroupAlreadyRemovedFromResourceException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupAlreadyRemovedFromResourceException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupAlreadyRemovedFromResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupAlreadyRemovedFromResourceException(Throwable cause) {
		super(cause);
	}

}
