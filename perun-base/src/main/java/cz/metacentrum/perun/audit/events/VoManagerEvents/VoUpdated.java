package cz.metacentrum.perun.audit.events.VoManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Vo;

public class VoUpdated extends AuditEvent {

  private Vo vo;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public VoUpdated() {
  }

  public VoUpdated(Vo vo) {
    this.vo = vo;
    this.message = formatMessage("%s updated.", vo);
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
    return message;
  }
}
