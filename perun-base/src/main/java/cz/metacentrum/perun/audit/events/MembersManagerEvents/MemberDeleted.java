package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;

public class MemberDeleted extends AuditEvent {

  private Member member;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public MemberDeleted() {
  }

  public MemberDeleted(Member member) {
    this.member = member;
    this.message = formatMessage("%s deleted.", member);
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
