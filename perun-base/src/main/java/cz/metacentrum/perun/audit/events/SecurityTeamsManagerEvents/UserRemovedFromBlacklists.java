package cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;

public class UserRemovedFromBlacklists extends AuditEvent {

	private final User user;
	private final String message;

	public UserRemovedFromBlacklists(User user) {
		this.user = user;
		this.message = String.format("%s remove from all blacklists.", user);
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
