package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;

public class GroupCreatedInVo extends AuditEvent {

  private Group group;
  private Vo vo;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public GroupCreatedInVo() {
  }

  public GroupCreatedInVo(Group group, Vo vo) {
    this.group = group;
    this.vo = vo;
    this.message = formatMessage("%s created in %s.", group, vo);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Group getGroup() {
    return group;
  }

  public Vo getVo() {
    return vo;
  }

  @Override
  public String toString() {
    return message;
  }
}
