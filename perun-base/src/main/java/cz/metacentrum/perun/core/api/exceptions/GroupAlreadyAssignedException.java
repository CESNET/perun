package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 * Checked version of GroupAlreadyAssignedException.
 *
 * @author Slavek Licehammer
 */
public class GroupAlreadyAssignedException extends PerunException {
	static final long serialVersionUID = 0;

	private Group group;

	public GroupAlreadyAssignedException(String message) {
		super(message);
	}

	public GroupAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupAlreadyAssignedException(Throwable cause) {
		super(cause);
	}

	public GroupAlreadyAssignedException(Group group) {
		super(group.toString());
		this.group = group;
	}

	public Group getGroup() {
		return group;
	}
}
