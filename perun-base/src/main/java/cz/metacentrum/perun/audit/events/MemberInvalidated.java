package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Member;

public class MemberInvalidated {
    private Member member;


    public MemberInvalidated(Member member) {
        this.member = member;
    }

    @Override
    public String toString() {
        return member + " invalidated.";
    }
}
