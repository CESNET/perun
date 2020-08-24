package cz.metacentrum.perun.audit.events.AuthorizationEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunBean;

public class RoleSetForGroup extends AuditEvent implements EngineIgnoreEvent {

	private PerunBean object;
	private Group group;
	private String role;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public RoleSetForGroup() {
	}

	public RoleSetForGroup(PerunBean object, Group group, String role) {
		this.object = object;
		this.group = group;
		this.role = role;
		this.message = formatMessage("Role %s was set for group %s on object %s.", role, group, object);
	}

	public PerunBean getObject() {
		return object;
	}

	public Group getGroup() {
		return group;
	}

	public String getRole() {
		return role;
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