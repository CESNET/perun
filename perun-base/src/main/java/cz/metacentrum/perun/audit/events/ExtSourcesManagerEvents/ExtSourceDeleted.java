package cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.ExtSource;

public class ExtSourceDeleted extends AuditEvent implements EngineIgnoreEvent {

  private ExtSource extSource;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ExtSourceDeleted() {
  }

  public ExtSourceDeleted(ExtSource extSource) {
    this.extSource = extSource;
    this.message = formatMessage("%s deleted.", extSource);
  }

  public ExtSource getExtSource() {
    return extSource;
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
