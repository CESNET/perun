package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class MemberExpiredInGroup extends AuditEvent {

  private Member member;
  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public MemberExpiredInGroup() {
  }

  public MemberExpiredInGroup(Member member, Group group) {
    this.member = member;
    this.group = group;
    this.message = formatMessage("%s in %s expired.", member, group);
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
}
