package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 * Checked version of GroupNotExistsException.
 *
 * @author Martin Kuba
 */
public class GroupNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Group group;

	public GroupNotExistsException(String message) {
		super(message);
	}

	public GroupNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupNotExistsException(Throwable cause) {
		super(cause);
	}

	public GroupNotExistsException(Group group) {
		super(group.toString());
		this.group = group;
	}

	public Group getGroup() {
		return this.group;
	}
}
