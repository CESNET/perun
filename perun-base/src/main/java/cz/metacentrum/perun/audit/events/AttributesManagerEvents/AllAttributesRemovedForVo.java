package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Vo;

public class AllAttributesRemovedForVo extends AuditEvent implements EngineIgnoreEvent {

  private Vo vo;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AllAttributesRemovedForVo() {
  }

  public AllAttributesRemovedForVo(Vo vo) {
    this.vo = vo;
    this.message = formatMessage("All attributes removed for %s.", vo);
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
