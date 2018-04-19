package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.core.api.Member;

public class MemberInvalidated {
    private Member member;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public MemberInvalidated(Member member) {
        this.member = member;
    }

    public MemberInvalidated() {
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return member + " invalidated.";
    }
}
