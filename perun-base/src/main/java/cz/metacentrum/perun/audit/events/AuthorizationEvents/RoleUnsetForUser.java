package cz.metacentrum.perun.audit.events.AuthorizationEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.User;

public class RoleUnsetForUser extends AuditEvent implements EngineIgnoreEvent {

	private PerunBean object;
	private User user;
	private String role;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public RoleUnsetForUser() {
	}

	public RoleUnsetForUser(PerunBean object, User user, String role) {
		this.object = object;
		this.user = user;
		this.role = role;
		this.message = formatMessage("Role %s was unset for user %s on object %s.", role, user, object);
	}

	public PerunBean getObject() {
		return object;
	}

	public User getUser() {
		return user;
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