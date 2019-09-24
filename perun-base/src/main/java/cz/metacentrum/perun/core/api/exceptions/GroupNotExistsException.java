package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 * This exception is thrown when trying to get a group which does not exist in the database
 *
 * @author Martin Kuba
 */
public class GroupNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Group group;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the group that does not exist
	 * @param group the group
	 */
	public GroupNotExistsException(Group group) {
		super(group.toString());
		this.group = group;
	}

	/**
	 * Getter for the group that does not exist
	 * @return the group that does not exist
	 */
	public Group getGroup() {
		return this.group;
	}
}
