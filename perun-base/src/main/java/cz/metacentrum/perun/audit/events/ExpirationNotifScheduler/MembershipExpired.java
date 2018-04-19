package cz.metacentrum.perun.audit.events.ExpirationNotifScheduler;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;

public class MembershipExpired {

    private Member member;
    private int daysAfterExpiration;
    private Vo vo;

    private String name = this.getClass().getName();
    private String message;

    public MembershipExpired(Member member, int daysAfterExpiration, Vo vo) {
        this.member = member;
        this.daysAfterExpiration = daysAfterExpiration;
        this.vo = vo;
    }

    public MembershipExpired() {
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public int getDaysAfterExpiration() {
        return daysAfterExpiration;
    }

    public void setDaysAfterExpiration(int daysAfterExpiration) {
        this.daysAfterExpiration = daysAfterExpiration;
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
        return member + " has expired " + daysAfterExpiration + " days ago in " + vo + ".";
    }
}
