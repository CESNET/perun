package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;

public class UserExtSourceRemovedFromUser extends AuditEvent {

	private final User user;
	private final UserExtSource userExtSource;
	private final String message;

	public UserExtSourceRemovedFromUser(UserExtSource userExtSource, User user) {
		this.userExtSource = userExtSource;
		this.user = user;
		this.message = String.format("%s removed from %s.", userExtSource, user);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public User getUser() {
		return user;
	}

	public UserExtSource getUserExtSource() {
		return userExtSource;
	}

	@Override
	public String toString() {
		return message;
	}
}
