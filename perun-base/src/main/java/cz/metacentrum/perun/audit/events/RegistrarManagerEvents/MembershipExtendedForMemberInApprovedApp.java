package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.registrar.model.Application;

public class MembershipExtendedForMemberInApprovedApp extends AuditEvent {

  private Member member;
  private Application app;
  private Vo vo;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public MembershipExtendedForMemberInApprovedApp() {
  }

  public MembershipExtendedForMemberInApprovedApp(Member member, Application app, Vo vo) {
    this.member = member;
    this.app = app;
    this.vo = vo;
    this.message = formatMessage("Membership extended for %s in %s for approved %s.", member, app.getVo(), app);
  }

  public Member getMember() {
    return member;
  }

  public Application getApp() {
    return app;
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
