package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class MemberValidatedInGroup extends AuditEvent {

  private Member member;
  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public MemberValidatedInGroup() {
  }

  public MemberValidatedInGroup(Member member, Group group) {
    this.member = member;
    this.group = group;
    this.message = formatMessage("%s in %s validated.", member, group);
  }

  public Member getMember() {
    return member;
  }

  public Group getGroup() {
    return group;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
