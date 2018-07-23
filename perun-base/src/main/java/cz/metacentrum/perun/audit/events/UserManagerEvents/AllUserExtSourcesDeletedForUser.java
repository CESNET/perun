package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.core.api.User;

public class AllUserExtSourcesDeletedForUser {

	private User user;
	private String name = this.getClass().getName();
	private String message;

	public AllUserExtSourcesDeletedForUser() {
	}

	public AllUserExtSourcesDeletedForUser(User user) {
		this.user = user;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "All user ext sources removed for " + user + ".";
	}
}
