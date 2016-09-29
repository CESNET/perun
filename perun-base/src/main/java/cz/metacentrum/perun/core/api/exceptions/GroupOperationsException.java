package cz.metacentrum.perun.core.api.exceptions;

/**
 * Exception thrown when operation with groups fails.
 * E.g. adding subgroup, creating/removing unions of groups etc.
 *
 * @author Oliver Mrázik
 */
public class GroupOperationsException extends PerunException {
	static final long serialVersionUID = 0;
	
	public GroupOperationsException() {
	}

	public GroupOperationsException(String message) {
		super(message);
	}

	public GroupOperationsException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public GroupOperationsException(Throwable cause) {
		super(cause);
	}
}