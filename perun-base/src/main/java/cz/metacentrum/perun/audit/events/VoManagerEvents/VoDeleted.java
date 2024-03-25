package cz.metacentrum.perun.audit.events.VoManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Vo;

public class VoDeleted extends AuditEvent implements EngineIgnoreEvent {

  private Vo vo;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public VoDeleted() {
  }

  public VoDeleted(Vo vo) {
    this.vo = vo;
    this.message = formatMessage("%s deleted.", vo);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Vo getVo() {
    return vo;
  }

  @Override
  public String toString() {
    return vo + " deleted.";
  }
}
