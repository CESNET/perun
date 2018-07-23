package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.core.api.User;

public class OwnershipDisabledForSpecificUser {

	private User user;
	private User specificUser;
	private String name = this.getClass().getName();
	private String message;

	public OwnershipDisabledForSpecificUser() {
	}

	public OwnershipDisabledForSpecificUser(User user, User specificUser) {
		this.user = user;
		this.specificUser = specificUser;
	}

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
		return user + " ownership was disabled for specificUser " + specificUser + ".";
	}
}
