package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the relation between groups should not exist but it exists
 *
 * @author Simona Kruppova
 */
public class GroupRelationAlreadyExists extends PerunException {

	/**
	 * Constructor with no arguments
	 */
	public GroupRelationAlreadyExists() {}

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupRelationAlreadyExists(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupRelationAlreadyExists(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupRelationAlreadyExists(Throwable cause) {
		super(cause);
	}
}
