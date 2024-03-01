package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Status;

public class MemberValidatedFailed extends AuditEvent implements EngineIgnoreEvent {

  private Member member;
  private Status status;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public MemberValidatedFailed() {
  }

  public MemberValidatedFailed(Member member, Status oldStatus) {
    this.member = member;
    this.status = oldStatus;
    this.message = formatMessage("Validation of %s failed. He stays in %s state.", member, status);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Member getMember() {
    return member;
  }

  public Status getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return message;
  }
}
