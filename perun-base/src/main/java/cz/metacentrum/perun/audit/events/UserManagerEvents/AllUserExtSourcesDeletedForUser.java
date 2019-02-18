package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.User;

public class AllUserExtSourcesDeletedForUser extends AuditEvent implements EngineIgnoreEvent {

	private User user;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AllUserExtSourcesDeletedForUser() {
	}

	public AllUserExtSourcesDeletedForUser(User user) {
		this.user = user;
		this.message = formatMessage("All user ext sources removed for %s.", user);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public User getUser() {
		return user;
	}

	@Override
	public String toString() {
		return message;
	}
}
