package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when trying to create a group whose vo_id, name and parent_group (if set) are identical to those of another group
 *
 * @author Martin Kuba
 */
public class GroupExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupExistsException(Throwable cause) {
		super(cause);
	}
}
