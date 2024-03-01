package cz.metacentrum.perun.audit.events.ExpirationNotifScheduler;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;

public class MembershipExpirationInDays extends AuditEvent implements EngineIgnoreEvent {

  private Member member;
  private int daysToExpiration;
  private Vo vo;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public MembershipExpirationInDays() {
  }

  public MembershipExpirationInDays(Member member, int daysToExpiration, Vo vo) {
    this.member = member;
    this.daysToExpiration = daysToExpiration;
    this.vo = vo;
    this.message = formatMessage("%s will expire in %d days in %s.", member, daysToExpiration, vo);
  }

  public Member getMember() {
    return member;
  }

  public int getDaysToExpiration() {
    return daysToExpiration;
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
