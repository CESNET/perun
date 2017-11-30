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

	public GroupMoveNotAllowedException() {}

	public GroupMoveNotAllowedException(String message) {
		super(message);
	}

	public GroupMoveNotAllowedException(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupMoveNotAllowedException(Throwable cause) {
		super(cause);
	}

	public GroupMoveNotAllowedException(String message, Group movingGroup, Group destinationGroup) {
		super(message);
		this.movingGroup = movingGroup;
		this.destinationGroup = destinationGroup;
	}

	public Group getMovingGroup() {
		return movingGroup;
	}

	public Group getDestinationGroup() {
		return destinationGroup;
	}
}
