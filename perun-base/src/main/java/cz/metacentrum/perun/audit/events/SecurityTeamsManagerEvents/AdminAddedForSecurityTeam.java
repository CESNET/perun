package cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;

public class AdminAddedForSecurityTeam extends AuditEvent {

	private User user;
	private SecurityTeam securityTeam;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AdminAddedForSecurityTeam() {
	}

	public AdminAddedForSecurityTeam(User user, SecurityTeam securityTeam) {
		this.user = user;
		this.securityTeam = securityTeam;
		this.message = formatMessage("%s was added as security admin of %s.", user, securityTeam);
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
