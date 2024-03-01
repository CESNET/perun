package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.UserExtSource;

public class AllAttributesRemovedForUserExtSource extends AuditEvent implements EngineIgnoreEvent {

  private UserExtSource userExtSource;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AllAttributesRemovedForUserExtSource() {
  }

  public AllAttributesRemovedForUserExtSource(UserExtSource userExtSource) {
    this.userExtSource = userExtSource;
    this.message = formatMessage("All attributes removed for %s.", userExtSource);
  }

  public UserExtSource getUserExtSource() {
    return userExtSource;
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
