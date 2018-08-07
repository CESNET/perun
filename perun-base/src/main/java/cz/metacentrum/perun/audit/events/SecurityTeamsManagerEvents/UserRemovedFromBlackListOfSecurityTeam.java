package cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;

public class UserRemovedFromBlackListOfSecurityTeam extends AuditEvent {

	private final User user;
	private final SecurityTeam securityTeam;
	private final String message;

	public UserRemovedFromBlackListOfSecurityTeam(User user, SecurityTeam securityTeam) {
		this.user = user;
		this.securityTeam = securityTeam;
		this.message = String.format("%s remove from blacklist of '%s'.", user, securityTeam);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public User getUser() {
		return user;
	}

	public SecurityTeam getSecurityTeam() {
		return securityTeam;
	}

	@Override
	public String toString() {
		return message;
	}
}
