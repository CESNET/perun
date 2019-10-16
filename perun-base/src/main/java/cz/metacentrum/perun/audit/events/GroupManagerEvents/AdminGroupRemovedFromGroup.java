package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;

public class AdminGroupRemovedFromGroup extends AuditEvent implements EngineIgnoreEvent {

	private Group group;
	private Group authorizedGroup;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AdminGroupRemovedFromGroup() {
	}

	public AdminGroupRemovedFromGroup(Group authorizedGroup, Group group) {
		this.group = group;
		this.authorizedGroup = authorizedGroup;
		this.message = formatMessage("%s was removed from admins of %s.", authorizedGroup, group);
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
