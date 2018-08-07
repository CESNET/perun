package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.UserExtSource;

public class UserExtSourceUpdated extends AuditEvent {

	private final UserExtSource userExtSource;
	private final String message;

	public UserExtSourceUpdated(UserExtSource userExtSource) {
		this.userExtSource = userExtSource;
		this.message = String.format("%s updated.", userExtSource);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public UserExtSource getUserExtSource() {
		return userExtSource;
	}

	@Override
	public String toString() {
		return message;
	}
}
