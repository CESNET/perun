package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Member;

public class MemberSuspended {

    private Member member;
    private String engineForceKeyword;

    public MemberSuspended(Member member, String engineForceKeyword) {
        this.member = member;
        this.engineForceKeyword = engineForceKeyword;
    }

    @Override
    public String toString() {
        return member + " suspended #" + engineForceKeyword +".";
    }
}
