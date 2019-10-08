package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when trying to remove the group from the resource with which the group is not associated
 *
 * @author Martin Kuba
 */
public class GroupNotDefinedOnResourceException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupNotDefinedOnResourceException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupNotDefinedOnResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupNotDefinedOnResourceException(Throwable cause) {
		super(cause);
	}
}
