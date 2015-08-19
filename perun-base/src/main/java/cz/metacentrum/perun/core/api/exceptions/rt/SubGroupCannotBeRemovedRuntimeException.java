package cz.metacentrum.perun.core.api.exceptions.rt;

import cz.metacentrum.perun.core.api.Group;

public class SubGroupCannotBeRemovedRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	private Group group;

	public SubGroupCannotBeRemovedRuntimeException() {
		super();
	}

	public SubGroupCannotBeRemovedRuntimeException(Group group) {
		super(group.toString());
		this.group = group;
	}

	public SubGroupCannotBeRemovedRuntimeException(Throwable cause) {
		super(cause);
	}

	public SubGroupCannotBeRemovedRuntimeException(Throwable cause, Group group) {
		super(group.toString(), cause);

		this.group = group;
	}

	public Group getGroup() {
		return group;
	}
}
