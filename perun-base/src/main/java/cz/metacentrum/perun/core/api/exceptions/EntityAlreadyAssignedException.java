package cz.metacentrum.perun.core.api.exceptions;

/**
 * It represents parent class for *AlreadyAssignedException classes.
 *
 * @author Slavek Licehammer
 */
public class EntityAlreadyAssignedException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public EntityAlreadyAssignedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public EntityAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public EntityAlreadyAssignedException(Throwable cause) {
		super(cause);
	}
}
