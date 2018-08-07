package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;


public class AllAttributesRemovedForGroup extends AuditEvent {

	private final Group group;
	private final String message;

	public AllAttributesRemovedForGroup(Group group) {
		this.group = group;
		this.message = String.format("All attributes removed for %s.", group);
	}

	public Group getGroup() {
		return group;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return message;
	}
}
