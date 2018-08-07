package cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;

public class UserAddedToBlackListOfSecurityTeam extends AuditEvent {

	private final User user;
	private final SecurityTeam securityTeam;
	private final String description;
	private final String message;

	public UserAddedToBlackListOfSecurityTeam(User user, SecurityTeam securityTeam, String description) {
		this.user = user;
		this.securityTeam = securityTeam;
		this.description = description;
		this.message = String.format("%s add to blacklist of %s with description '%s'.", user, securityTeam, description);
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

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return message;
	}
}
