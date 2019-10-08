package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 * This exception is thrown when the group is not admin of: facility, another group, resource, security team, ...
 * @author Jiří Mauritz
 */
public class GroupNotAdminException extends PerunException {
	static final long serialVersionUID = 0;

	private Group group;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupNotAdminException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupNotAdminException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupNotAdminException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the group which is not admin
	 * @param group the group which is not admin
	 */
	public GroupNotAdminException(Group group) {
		super(group.toString());
		this.group = group;
	}

	/**
	 * Getter for the group
	 * @return group which is not admin
	 */
	public Group getGroup() {
		return group;
	}
}
