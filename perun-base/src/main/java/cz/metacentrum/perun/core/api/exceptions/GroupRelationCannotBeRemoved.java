package cz.metacentrum.perun.core.api.exceptions;

/**
 * Exception thrown whem group relation cannot be removed.
 * f.e. when it's part of the hierarchical structure of the groups
 *
 * @author Simona Kruppova
 */
public class GroupRelationCannotBeRemoved extends PerunException {

	public GroupRelationCannotBeRemoved() {}

	public GroupRelationCannotBeRemoved(String message) {
		super(message);
	}

	public GroupRelationCannotBeRemoved(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupRelationCannotBeRemoved(Throwable cause) {
		super(cause);
	}
}
