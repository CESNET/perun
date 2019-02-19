package cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.SecurityTeam;

public class AdminGroupAddedForSecurityTeam extends AuditEvent implements EngineIgnoreEvent {

	private Group group;
	private SecurityTeam securityTeam;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AdminGroupAddedForSecurityTeam() {
	}

	public AdminGroupAddedForSecurityTeam(Group group, SecurityTeam securityTeam) {
		this.group = group;
		this.securityTeam = securityTeam;
		this.message = formatMessage("%s was added as security admins of %s.", group, securityTeam);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Group getGroup() {
		return group;
	}

	public SecurityTeam getSecurityTeam() {
		return securityTeam;
	}

	@Override
	public String toString() {
		return message;
	}
}
