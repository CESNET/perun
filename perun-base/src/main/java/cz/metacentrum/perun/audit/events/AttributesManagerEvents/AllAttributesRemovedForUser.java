package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;

public class AllAttributesRemovedForUser extends AuditEvent {

	private User user;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AllAttributesRemovedForUser() {
	}

	public AllAttributesRemovedForUser(User user) {
		this.user = user;
		this.message = formatMessage("All attributes removed for %s.", user);
	}

	public User getUser() {
		return user;
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
