package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 * This exception is thrown when the synchronization for the group is already running
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.GroupSynchronizationAlreadyRunningException
 * @author Michal Prochazka
 */
public class GroupSynchronizationAlreadyRunningException extends PerunException {
	static final long serialVersionUID = 0;

	private Group group;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupSynchronizationAlreadyRunningException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupSynchronizationAlreadyRunningException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupSynchronizationAlreadyRunningException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the group
	 * @param group group for which the synchronization is already running
	 */
	public GroupSynchronizationAlreadyRunningException(Group group) {
		super(group.toString());
		this.group = group;
	}

	/**
	 * Getter for the group
	 * @return group for which the synchronization is already running
	 */
	public Group getGroup() {
		return this.group;
	}
}
