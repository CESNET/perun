package cz.metacentrum.perun.core.api.exceptions;

/**
 * Exception thrown when the group relation cannot be created, because it's not allowed
 * f.e. it would create cycle, one of the groups is members group...
 *
 * @author Simona Kruppova
 */
public class GroupRelationNotAllowed extends PerunException {

	/**
	 * Constructor without arguments
	 */
	public GroupRelationNotAllowed() {}

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupRelationNotAllowed(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupRelationNotAllowed(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupRelationNotAllowed(Throwable cause) {
		super(cause);
	}
}
