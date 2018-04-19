package cz.metacentrum.perun.audit.events.ExpirationNotifScheduler;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;

public class MembershipExpirationInMonthNotification {

    private Member member;
    private Vo vo;

    private String name = this.getClass().getName();
    private String message;

    public MembershipExpirationInMonthNotification(Member member, Vo vo) {
        this.member = member;
        this.vo = vo;
    }

    public MembershipExpirationInMonthNotification() {
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
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
        return String.format("%s will expire in a month in %s." ,member,vo);
    }
}
