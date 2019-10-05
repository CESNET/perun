package cz.metacentrum.perun.core.api.exceptions;

/**
 * Exception thrown when group relation cannot be removed.
 * f.e. when it's part of the hierarchical structure of the groups
 *
 * @author Simona Kruppova
 */
public class GroupRelationCannotBeRemoved extends PerunException {

	/**
	 * Constructor without arguments
	 */
	public GroupRelationCannotBeRemoved() {}

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupRelationCannotBeRemoved(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupRelationCannotBeRemoved(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupRelationCannotBeRemoved(Throwable cause) {
		super(cause);
	}
}
