package cz.metacentrum.perun.audit.events.ExpirationNotifScheduler;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

public class GroupMembershipExpirationInDays extends AuditEvent implements EngineIgnoreEvent {

  private Member member;
  private int daysToExpiration;
  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public GroupMembershipExpirationInDays() {
  }

  public GroupMembershipExpirationInDays(Member member, int daysToExpiration, Group group) {
    this.member = member;
    this.daysToExpiration = daysToExpiration;
    this.group = group;
    this.message = formatMessage("%s will expire in %d days in %s.", member, daysToExpiration, group);
  }

  public Member getMember() {
    return member;
  }

  public int getDaysToExpiration() {
    return daysToExpiration;
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
