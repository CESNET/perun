package cz.metacentrum.perun.audit.events.AuthorshipManagementEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.cabinet.model.Authorship;

public class AuthorshipDeleted extends AuditEvent implements EngineIgnoreEvent {

  private Authorship authorship;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AuthorshipDeleted() {
  }

  public AuthorshipDeleted(Authorship authorship) {
    this.authorship = authorship;
    this.message = formatMessage("Authorship %s deleted.", authorship.serializeToString());
  }

  public Authorship getAuthorship() {
    return authorship;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return message;
  }
}
