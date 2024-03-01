package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineForceEvent;
import cz.metacentrum.perun.core.api.Member;

public class MemberSuspended extends AuditEvent implements EngineForceEvent {

  private Member member;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public MemberSuspended() {
  }

  public MemberSuspended(Member member) {
    this.member = member;
    this.message = formatMessage("%s suspended.", member);
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
