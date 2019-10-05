package cz.metacentrum.perun.core.api.exceptions;

/**
 * Exception thrown when relation between groups should exist but it does not
 *
 * @author Simona Kruppova
 */
public class GroupRelationDoesNotExist extends PerunException {

	/**
	 * Constructor without arguments
	 */
	public GroupRelationDoesNotExist() {}

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupRelationDoesNotExist(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupRelationDoesNotExist(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupRelationDoesNotExist(Throwable cause) {
		super(cause);
	}
}
