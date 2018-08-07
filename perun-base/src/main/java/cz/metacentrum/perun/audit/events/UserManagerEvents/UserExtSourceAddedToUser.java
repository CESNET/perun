package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;

public class UserExtSourceAddedToUser extends AuditEvent {

	private final UserExtSource userExtSource;
	private final User user;
	private final String message;

	public UserExtSourceAddedToUser(UserExtSource userExtSource, User user) {
		this.user = user;
		this.userExtSource = userExtSource;
		this.message = String.format("%s added to %s.", userExtSource, user);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public UserExtSource getUserExtSource() {
		return userExtSource;
	}

	public User getUser() {
		return user;
	}

	@Override
	public String toString() {
		return message;
	}
}
