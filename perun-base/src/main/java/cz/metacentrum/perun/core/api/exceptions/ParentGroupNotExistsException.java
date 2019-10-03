package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the parent group for the group does not exist
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.GroupExistsRuntimeException
 * @author Martin Kuba
 */
public class ParentGroupNotExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ParentGroupNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ParentGroupNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ParentGroupNotExistsException(Throwable cause) {
		super(cause);
	}
}
