package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.registrar.model.Application;

public class MembershipExtendedForMemberInApprovedApp {

    private Member member;
    private Application app;
    private Vo vo;
    private String name = this.getClass().getName();
    private String message;

    public MembershipExtendedForMemberInApprovedApp() {
    }

    public MembershipExtendedForMemberInApprovedApp(Member member, Application app, Vo vo) {
        this.member = member;
        this.app = app;
        this.vo = vo;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Application getApp() {
        return app;
    }

    public void setApp(Application app) {
        this.app = app;
    }

    public Vo getVo() {
        return vo;
    }

    public void setVo(Vo vo) {
        this.vo = vo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("Membership extended for {} in {} for approved {}.", member, app.getVo(), app);
    }
}
