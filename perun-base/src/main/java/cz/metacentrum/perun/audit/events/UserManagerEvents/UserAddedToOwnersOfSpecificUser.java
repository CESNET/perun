package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;

public class UserAddedToOwnersOfSpecificUser extends AuditEvent {

	private User user;
	private User specificUser;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public UserAddedToOwnersOfSpecificUser() {
	}

	public UserAddedToOwnersOfSpecificUser(User user, User specificUser) {
		this.user = user;
		this.specificUser = specificUser;
		this.message = formatMessage("%s was added to owners of %s.", user, specificUser);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public User getUser() {
		return user;
	}

	public User getSpecificUser() {
		return specificUser;
	}

	@Override
	public String toString() {
		return message;
	}
}
