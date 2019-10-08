package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 * Exception thrown when the group cannot be moved, because it's not allowed
 * f.e. Destination group is subGroup of Moving group, Moving group is already in destination group as subGroup...
 *
 * @author Peter Balcirak
 */
public class GroupMoveNotAllowedException extends PerunException {

	private Group movingGroup;
	private Group destinationGroup;

	/**
	 * Constructor with no arguments
	 */
	public GroupMoveNotAllowedException() {}

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public GroupMoveNotAllowedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupMoveNotAllowedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public GroupMoveNotAllowedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with a message, group to be moved, destination group (the group to which the moving group is moved)
	 * @param message message with the details
	 * @param movingGroup group to be moved
	 * @param destinationGroup the group to which the moving group is moved
	 */
	public GroupMoveNotAllowedException(String message, Group movingGroup, Group destinationGroup) {
		super(message);
		this.movingGroup = movingGroup;
		this.destinationGroup = destinationGroup;
	}

	/**
	 * Getter for the moving group
	 * @return group to be moved
	 */
	public Group getMovingGroup() {
		return movingGroup;
	}

	/**
	 * The group to which the moving group is moved
	 * @return destination group
	 */
	public Group getDestinationGroup() {
		return destinationGroup;
	}
}
