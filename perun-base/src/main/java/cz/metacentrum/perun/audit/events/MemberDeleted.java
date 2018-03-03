package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Member;

public class MemberDeleted {

    private Member member;


    public MemberDeleted(Member member) {
        this.member = member;
    }

    @Override
    public String toString() {
        return member + " deleted.";
    }
}
