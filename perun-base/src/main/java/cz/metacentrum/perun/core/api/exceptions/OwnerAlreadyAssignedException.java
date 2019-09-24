package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the owner has already been assigned to the facility
 *
 * @author Slavek Licehammer
 */
public class OwnerAlreadyAssignedException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public OwnerAlreadyAssignedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public OwnerAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public OwnerAlreadyAssignedException(Throwable cause) {
		super(cause);
	}

}
