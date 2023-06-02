package cz.metacentrum.perun.core.api.exceptions;

/**
 * Exception thrown when relation of two groups as parent-sub should exist, but does not.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class GroupIsNotASubgroupException extends PerunException {

	/**
	 * Constructor without arguments
	 */
	public GroupIsNotASubgroupException() {}

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupIsNotASubgroupException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupIsNotASubgroupException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupIsNotASubgroupException(Throwable cause) {
		super(cause);
	}
}
