package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;

public class GroupMoved extends AuditEvent {

	private final Group group;
	private final String message;

	public GroupMoved(Group group) {
		this.group = group;
		this.message = String.format("Group %s was moved.", group);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Group getGroup() {
		return group;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return message;
	}
}
