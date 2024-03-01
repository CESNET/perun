package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;

public class MemberValidated extends AuditEvent {

  private Member member;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public MemberValidated() {
  }

  public MemberValidated(Member member) {
    this.member = member;
    this.message = formatMessage("%s validated.", member);
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
