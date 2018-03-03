package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Member;

public class MemberDisabled {
    private Member member;


    public MemberDisabled(Member member) {
        this.member = member;

    }

    @Override
    public String toString() {
        return member + " disabled.";
    }
}
