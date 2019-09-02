package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ExtSourceNotAssignedException.
 * Exception is thrown when the ExtSource has not been assigned.
 *
 * @author Slavek Licehammer
 */
public class ExtSourceNotAssignedException extends EntityNotAssignedException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ExtSourceNotAssignedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtSourceNotAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtSourceNotAssignedException(Throwable cause) {
		super(cause);
	}
}
