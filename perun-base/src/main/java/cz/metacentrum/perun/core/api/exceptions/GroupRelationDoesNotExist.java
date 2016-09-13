package cz.metacentrum.perun.core.api.exceptions;

/**
 * Exception thrown when relation should exist but it does not
 *
 * @author Simona Kruppova
 */
public class GroupRelationDoesNotExist extends PerunException {

	public GroupRelationDoesNotExist() {}

	public GroupRelationDoesNotExist(String message) {
		super(message);
	}

	public GroupRelationDoesNotExist(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupRelationDoesNotExist(Throwable cause) {
		super(cause);
	}
}
