package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;

public class UserAddedToOwnersOfSpecificUser implements AuditEvent {

	private User user;
	private User specificUser;
	private String name = this.getClass().getName();
	private String message;

	public UserAddedToOwnersOfSpecificUser() {
	}

	public UserAddedToOwnersOfSpecificUser(User user, User specificUser) {
		this.user = user;
		this.specificUser = specificUser;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getSpecificUser() {
		return specificUser;
	}

	public void setSpecificUser(User specificUser) {
		this.specificUser = specificUser;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return user + " was added to owners of " + specificUser + ".";
	}
}
