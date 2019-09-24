package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 * This exception is thrown when the group has already been assigned to the specific resource
 *
 * @author Slavek Licehammer
 */
public class GroupAlreadyAssignedException extends PerunException {
	static final long serialVersionUID = 0;

	private Group group;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupAlreadyAssignedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupAlreadyAssignedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the group
	 * @param group that has already been assigned
	 */
	public GroupAlreadyAssignedException(Group group) {
		super(group.toString());
		this.group = group;
	}

	/**
	 * Getter for the group
	 * @return group that has already been assigned
	 */
	public Group getGroup() {
		return group;
	}
}
