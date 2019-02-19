package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;

public class AdminRemovedForGroup extends AuditEvent implements EngineIgnoreEvent {

	private User user;
	private Group group;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AdminRemovedForGroup() {
	}

	public AdminRemovedForGroup(User user, Group group) {
		this.user = user;
		this.group = group;
		this.message = formatMessage("%s was removed from admins of %s.", user, group);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public User getUser() {
		return user;
	}

	public Group getGroup() {
		return group;
	}

	@Override
	public String toString() {
		return message;
	}
}
