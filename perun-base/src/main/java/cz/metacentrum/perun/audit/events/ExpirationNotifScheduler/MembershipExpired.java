package cz.metacentrum.perun.audit.events.ExpirationNotifScheduler;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;

public class MembershipExpired extends AuditEvent implements EngineIgnoreEvent {

  private Member member;
  private int daysAfterExpiration;
  private Vo vo;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public MembershipExpired() {
  }

  public MembershipExpired(Member member, int daysAfterExpiration, Vo vo) {
    this.member = member;
    this.daysAfterExpiration = daysAfterExpiration;
    this.vo = vo;
    this.message = formatMessage("%s has expired %d days ago in %s.", member, daysAfterExpiration, vo);
  }

  public Member getMember() {
    return member;
  }

  public int getDaysAfterExpiration() {
    return daysAfterExpiration;
  }

  public Vo getVo() {
    return vo;
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
