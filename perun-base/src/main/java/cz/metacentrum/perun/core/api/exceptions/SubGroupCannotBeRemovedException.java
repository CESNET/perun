package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 * Thrown when the subGroup cannot be removed
 *
 * @author Slavek Licehammer
 */
public class SubGroupCannotBeRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	private Group group;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public SubGroupCannotBeRemovedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SubGroupCannotBeRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SubGroupCannotBeRemovedException(Throwable cause) {
		super(cause);
	}

	public SubGroupCannotBeRemovedException(Group group) {
		super(group.toString());
		this.group = group;
	}

	public Group getGroup() {
		return group;
	}
}
