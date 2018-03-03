package cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;

public class UserRemovedFromBlacklists implements AuditEvent {

	private User user;
	private String name = this.getClass().getName();
	private String message;

	public UserRemovedFromBlacklists() {
	}

	public UserRemovedFromBlacklists(User user) {
		this.user = user;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return user + " remove from all blacklists.";
	}
}
