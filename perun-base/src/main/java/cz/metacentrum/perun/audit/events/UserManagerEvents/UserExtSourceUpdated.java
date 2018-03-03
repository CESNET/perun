package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.UserExtSource;

public class UserExtSourceUpdated implements AuditEvent {

	private UserExtSource userExtSource;
	private String name = this.getClass().getName();
	private String message;

	public UserExtSourceUpdated() {
	}

	public UserExtSourceUpdated(UserExtSource userExtSource) {
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return userExtSource + " updated.";
	}
}
