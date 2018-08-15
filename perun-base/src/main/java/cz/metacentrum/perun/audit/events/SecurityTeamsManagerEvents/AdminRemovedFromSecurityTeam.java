package cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;

public class AdminRemovedFromSecurityTeam extends AuditEvent {

	private User user;
	private SecurityTeam securityTeam;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AdminRemovedFromSecurityTeam() {
	}

	public AdminRemovedFromSecurityTeam(User user, SecurityTeam securityTeam) {
		this.user = user;
		this.securityTeam = securityTeam;
		this.message = formatMessage("%s was removed from security admins of %s.", user, securityTeam);
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
