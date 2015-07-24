package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 *
 * @author Jiří Mauritz
 */
public class GroupNotAdminException extends PerunException {
	static final long serialVersionUID = 0;

	private Group group;

	public GroupNotAdminException(String message) {
		super(message);
	}

	public GroupNotAdminException(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupNotAdminException(Throwable cause) {
		super(cause);
	}

	public GroupNotAdminException(Group group) {
		super(group.toString());
		this.group = group;
	}

	public Group getGroup() {
		return group;
	}
}
