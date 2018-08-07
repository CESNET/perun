package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;

public class GroupUpdated extends AuditEvent {

	private final Group group;
	private final String message;

	public GroupUpdated(Group group) {
		this.group = group;
		this.message = String.format("%s updated.", group);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Group getGroup() {
		return group;
	}

	@Override
	public String toString() {
		return message;
	}
}
