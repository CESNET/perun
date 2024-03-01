package cz.metacentrum.perun.audit.events.ExpirationNotifScheduler;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

public class GroupMembershipExpirationInMonthNotification extends AuditEvent implements EngineIgnoreEvent {

  private Member member;
  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public GroupMembershipExpirationInMonthNotification() {
  }

  public GroupMembershipExpirationInMonthNotification(Member member, Group group) {
    this.member = member;
    this.group = group;
    this.message = formatMessage("%s will expire in a month in %s.", member, group);
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

  @Override
  public String toString() {
    return message;
  }
}
