package cz.metacentrum.perun.core.api.exceptions;

/**
 * Exception thrown when relation should not exist but it exists
 *
 * @author Simona Kruppova
 */
public class GroupRelationAlreadyExists extends PerunException {

	public GroupRelationAlreadyExists() {}

	public GroupRelationAlreadyExists(String message) {
		super(message);
	}

	public GroupRelationAlreadyExists(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupRelationAlreadyExists(Throwable cause) {
		super(cause);
	}
}
