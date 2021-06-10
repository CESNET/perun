package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when it is not possible to perform an action on
 * a group-resource assignment because of its status.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class GroupResourceStatusException extends PerunException {

	/**
	 * Simple constructor with a message.
	 *
	 * @param message message with details about the cause
	 */
	public GroupResourceStatusException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object.
	 *
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupResourceStatusException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object.
	 *
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupResourceStatusException(Throwable cause) {
		super(cause);
	}
}