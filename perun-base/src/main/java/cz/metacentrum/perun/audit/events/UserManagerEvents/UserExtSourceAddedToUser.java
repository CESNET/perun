package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;

public class UserExtSourceAddedToUser implements AuditEvent {

	private UserExtSource userExtSource;
	private User user;
	private String name = this.getClass().getName();
	private String message;

	public UserExtSourceAddedToUser() {
	}

	public UserExtSourceAddedToUser(UserExtSource userExtSource, User user) {
		this.user = user;
		this.userExtSource = userExtSource;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public UserExtSource getUserExtSource() {
		return userExtSource;
	}

	public void setUserExtSource(UserExtSource userExtSource) {
		this.userExtSource = userExtSource;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return userExtSource + " added to " + user + ".";
	}
}
