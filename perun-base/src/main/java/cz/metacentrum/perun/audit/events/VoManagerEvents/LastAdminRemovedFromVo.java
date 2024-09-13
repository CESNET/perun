package cz.metacentrum.perun.audit.events.VoManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Vo;

public class LastAdminRemovedFromVo extends AuditEvent {
  private Vo vo;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public LastAdminRemovedFromVo() {
  }

  public LastAdminRemovedFromVo(Vo vo) {
    this.vo = vo;
    this.message = formatMessage("Last admin removed from %s", vo);
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
