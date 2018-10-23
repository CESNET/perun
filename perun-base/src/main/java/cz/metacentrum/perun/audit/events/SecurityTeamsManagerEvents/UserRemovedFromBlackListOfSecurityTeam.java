package cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;

public class UserRemovedFromBlackListOfSecurityTeam implements AuditEvent {

	private User user;
	private SecurityTeam securityTeam;
	private String name = this.getClass().getName();
	private String message;

	public UserRemovedFromBlackListOfSecurityTeam() {
	}

	public UserRemovedFromBlackListOfSecurityTeam(User user, SecurityTeam securityTeam) {

		this.user = user;
		this.securityTeam = securityTeam;
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

	public SecurityTeam getSecurityTeam() {
		return securityTeam;
	}

	public void setSecurityTeam(SecurityTeam securityTeam) {
		this.securityTeam = securityTeam;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return user + " remove from blacklist of " + securityTeam + ".";
	}
}
