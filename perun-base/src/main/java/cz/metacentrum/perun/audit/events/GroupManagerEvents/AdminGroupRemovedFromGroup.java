package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;

public class AdminGroupRemovedFromGroup extends AuditEvent {

	private final Group group;
	private final Group authorizedGroup;
	private final String message;

	public AdminGroupRemovedFromGroup(Group authorizedGroup, Group group) {
		this.group = group;
		this.authorizedGroup = authorizedGroup;
		this.message = String.format("Group %s was removed from admins of %s.", authorizedGroup, group);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Group getGroup() {
		return group;
	}

	public Group getAuthorizedGroup() {
		return authorizedGroup;
	}

	@Override
	public String toString() {
		return message;
	}
}
