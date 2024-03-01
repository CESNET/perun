package cz.metacentrum.perun.audit.events.ExpirationNotifScheduler;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

public class GroupMembershipExpired extends AuditEvent implements EngineIgnoreEvent {

  private Member member;
  private int daysAfterExpiration;
  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public GroupMembershipExpired() {
  }

  public GroupMembershipExpired(Member member, int daysAfterExpiration, Group group) {
    this.member = member;
    this.daysAfterExpiration = daysAfterExpiration;
    this.group = group;
    this.message = formatMessage("%s has expired %d days ago in %s.", member, daysAfterExpiration, group);
  }

  public Member getMember() {
    return member;
  }

  public int getDaysAfterExpiration() {
    return daysAfterExpiration;
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
