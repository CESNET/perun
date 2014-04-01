package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 * Service not exists in underlaying data source.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.GroupSynchronizationAlreadyRunningException
 * @author Michal Prochazka
 */
public class GroupSynchronizationAlreadyRunningException extends PerunException {
	static final long serialVersionUID = 0;

	private Group group;

	public GroupSynchronizationAlreadyRunningException(String message) {
		super(message);
	}

	public GroupSynchronizationAlreadyRunningException(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupSynchronizationAlreadyRunningException(Throwable cause) {
		super(cause);
	}

	public GroupSynchronizationAlreadyRunningException(Group group) {
		super(group.toString());
		this.group = group;
	}

	public Group getGroup() {
		return this.group;
	}
}
