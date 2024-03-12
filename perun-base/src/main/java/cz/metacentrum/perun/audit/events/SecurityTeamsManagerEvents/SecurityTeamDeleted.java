package cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.SecurityTeam;

public class SecurityTeamDeleted extends AuditEvent implements EngineIgnoreEvent {

  private SecurityTeam securityTeam;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public SecurityTeamDeleted() {
  }

  public SecurityTeamDeleted(SecurityTeam securityTeam) {
    this.securityTeam = securityTeam;
    this.message = formatMessage("%s was deleted.", securityTeam);
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
