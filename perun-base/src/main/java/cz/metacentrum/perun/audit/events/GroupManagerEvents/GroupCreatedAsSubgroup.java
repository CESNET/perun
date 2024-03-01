package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;

public class GroupCreatedAsSubgroup extends AuditEvent {

  private Group group;
  private Group parentGroup;
  private Vo vo;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public GroupCreatedAsSubgroup() {
  }

  public GroupCreatedAsSubgroup(Group group, Vo vo, Group parentGroup) {
    this.group = group;
    this.parentGroup = parentGroup;
    this.vo = vo;
    this.message = formatMessage("%s created in %s as subgroup of %s", group, vo, parentGroup);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Group getGroup() {
    return group;
  }

  public Group getParentGroup() {
    return parentGroup;
  }

  public Vo getVo() {
    return vo;
  }

  @Override
  public String toString() {
    return message;
  }
}
