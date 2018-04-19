package cz.metacentrum.perun.audit.events.ExpirationNotifScheduler;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;

public class MembershipExpirationInDays {


    private Member member;
    private int daysToExpiration;
    private Vo vo;

    private String name = this.getClass().getName();
    private String message;

    public MembershipExpirationInDays(Member member, int daysToExpiration, Vo vo) {
        this.member = member;
        this.daysToExpiration = daysToExpiration;
        this.vo = vo;
    }

    public MembershipExpirationInDays() {
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public int getDaysToExpiration() {
        return daysToExpiration;
    }

    public void setDaysToExpiration(int daysToExpiration) {
        this.daysToExpiration = daysToExpiration;
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
        return String.format("%s will expire in %s days in %d." ,member,vo,daysToExpiration);
    }
}
