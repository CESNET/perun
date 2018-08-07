package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.UserExtSource;

public class AllAttributesRemovedForUserExtSource extends AuditEvent {

	private final UserExtSource userExtSource;
	private final String message;

	public AllAttributesRemovedForUserExtSource(UserExtSource userExtSource) {
		this.userExtSource = userExtSource;
		this.message = String.format("All attributes removed for %s.", userExtSource);
	}

	public UserExtSource getUserExtSource() {
		return userExtSource;
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
