package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

public class DirectMemberAddedToGroup extends AuditEvent {

  private Group group;
  private Member member;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public DirectMemberAddedToGroup() {
  }

  public DirectMemberAddedToGroup(Member member, Group group) {
    this.group = group;
    this.member = member;
    this.message = formatMessage("%s added to %s", member, group);
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

