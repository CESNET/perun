package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Member;

public class MemberValidated {

    private Member member;


    public MemberValidated(Member member) {
        this.member = member;
    }

    @Override
    public String toString() {
        return member + " validated.";
    }
}
