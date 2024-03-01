package cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Vo;

public class ExtSourceRemovedFromVo extends AuditEvent implements EngineIgnoreEvent {

  private ExtSource source;
  private Vo vo;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ExtSourceRemovedFromVo() {
  }

  public ExtSourceRemovedFromVo(ExtSource source, Vo vo) {
    this.source = source;
    this.vo = vo;
    this.message = formatMessage("%s removed from %s.", source, vo);
  }

  public ExtSource getSource() {
    return source;
  }

  public Vo getVo() {
    return vo;
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
