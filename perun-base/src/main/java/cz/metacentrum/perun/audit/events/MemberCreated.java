package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Member;

public class MemberCreated {

    private Member member;


    public MemberCreated(Member member) {
        this.member = member;
    }

    @Override
    public String toString() {
        return member + " created.";
    }
}
