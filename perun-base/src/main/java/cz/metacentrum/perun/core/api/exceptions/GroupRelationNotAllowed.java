package cz.metacentrum.perun.core.api.exceptions;

/**
 * Exception thrown when the group relation cannot be created, because it's not allowed
 * f.e. it would create cycle, one of the groups is members group...
 *
 * @author Simona Kruppova
 */
public class GroupRelationNotAllowed extends PerunException {

	public GroupRelationNotAllowed() {}

	public GroupRelationNotAllowed(String message) {
		super(message);
	}

	public GroupRelationNotAllowed(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupRelationNotAllowed(Throwable cause) {
		super(cause);
	}
}
