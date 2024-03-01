package cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.SecurityTeam;

public class SecurityTeamCreated extends AuditEvent implements EngineIgnoreEvent {

  private SecurityTeam securityTeam;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public SecurityTeamCreated() {
  }

  public SecurityTeamCreated(SecurityTeam securityTeam) {
    this.securityTeam = securityTeam;
    this.message = formatMessage("%s was created.", securityTeam);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public SecurityTeam getSecurityTeam() {
    return securityTeam;
  }

  @Override
  public String toString() {
    return message;
  }
}
