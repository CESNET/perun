package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.Member;

public class AllAttributesRemovedForMember {

    private Member member;
    private String name = this.getClass().getName();
    private String message;

    public AllAttributesRemovedForMember(Member member) {
        this.member = member;
    }

    public AllAttributesRemovedForMember() {
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

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("All attributes removed for %s.",member);
    }
}
