package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 * This exception is thrown when the group structure synchronization is already running for given group
 *
 * @author Peter Balcirak peter.balcirak@gmail.com
 */
public class GroupStructureSynchronizationAlreadyRunningException extends PerunException {
	static final long serialVersionUID = 0;

	private Group group;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupStructureSynchronizationAlreadyRunningException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupStructureSynchronizationAlreadyRunningException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupStructureSynchronizationAlreadyRunningException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the group
	 * @param group the group for which the synchronization is already running
	 */
	public GroupStructureSynchronizationAlreadyRunningException(Group group) {
		super("Group structure is already running for the group: " + group.toString());
		this.group = group;
	}

	/**
	 * Getter for the group
	 * @return the group for which the synchronization is already running
	 */
	public Group getGroup() {
		return this.group;
	}
}
