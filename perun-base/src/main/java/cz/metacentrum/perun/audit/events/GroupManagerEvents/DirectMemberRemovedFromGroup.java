package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

public class DirectMemberRemovedFromGroup extends AuditEvent {

  private Member member;
  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public DirectMemberRemovedFromGroup() {
  }

  public DirectMemberRemovedFromGroup(Member member, Group group) {
    this.member = member;
    this.group = group;
    this.message = formatMessage("%s was removed from %s.", member, group);
  }

  public Group getGroup() {
    return group;
  }

  public Member getMember() {
    return member;
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
