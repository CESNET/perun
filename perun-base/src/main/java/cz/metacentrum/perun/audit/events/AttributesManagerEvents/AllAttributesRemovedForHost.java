package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Host;

public class AllAttributesRemovedForHost extends AuditEvent implements EngineIgnoreEvent {

  private Host host;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AllAttributesRemovedForHost() {
  }

  public AllAttributesRemovedForHost(Host host) {
    this.host = host;
    this.message = formatMessage("All attributes removed for %s.", host);
  }

  public Host getHost() {
    return host;
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
