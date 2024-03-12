package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Vo;

public class AllGroupsFromVoDeleted extends AuditEvent {

  private Vo vo;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AllGroupsFromVoDeleted() {
  }

  public AllGroupsFromVoDeleted(Vo vo) {
    this.vo = vo;
    this.message = formatMessage("All group in %s deleted.", vo);
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
