package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;

public class MemberInvalidated extends AuditEvent {

  private Member member;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public MemberInvalidated() {
  }

  public MemberInvalidated(Member member) {
    this.member = member;
    this.message = formatMessage("%s invalidated.", member);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Member getMember() {
    return member;
  }

  @Override
  public String toString() {
    return message;
  }
}
