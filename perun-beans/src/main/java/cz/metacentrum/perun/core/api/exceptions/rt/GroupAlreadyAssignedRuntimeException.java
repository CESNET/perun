package cz.metacentrum.perun.core.api.exceptions.rt;

import cz.metacentrum.perun.core.api.Group;

public class GroupAlreadyAssignedRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	private Group group;

	public GroupAlreadyAssignedRuntimeException() {
		super();
	}

	public GroupAlreadyAssignedRuntimeException(Group group) {
		super(group.toString());
		this.group = group;
	}

	public GroupAlreadyAssignedRuntimeException(Throwable cause) {
		super(cause);
	}

	public GroupAlreadyAssignedRuntimeException(Throwable cause, Group group) {
		super(group.toString(), cause);

		this.group = group;
	}

	public Group getGroup() {
		return group;
	}
}
