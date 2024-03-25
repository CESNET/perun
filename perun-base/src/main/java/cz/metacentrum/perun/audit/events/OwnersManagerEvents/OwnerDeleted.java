package cz.metacentrum.perun.audit.events.OwnersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Owner;

public class OwnerDeleted extends AuditEvent implements EngineIgnoreEvent {

  private Owner owner;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public OwnerDeleted() {
  }

  public OwnerDeleted(Owner owner) {
    this.owner = owner;
    this.message = formatMessage("%s deleted.", owner);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Owner getOwner() {
    return owner;
  }

  @Override
  public String toString() {
    return message;
  }
}
