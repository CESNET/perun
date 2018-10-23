package cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.SecurityTeam;

public class SecurityTeamCreated implements AuditEvent {

	private SecurityTeam securityTeam;
	private String name = this.getClass().getName();
	private String message;

	public SecurityTeamCreated() {
	}

	public SecurityTeamCreated(SecurityTeam securityTeam) {
		this.securityTeam = securityTeam;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
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
		return securityTeam + " was created.";
	}
}
