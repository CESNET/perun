package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

public class IndirectMemberRemovedFromGroup {

    private Member member;
    private Group group;

    public IndirectMemberRemovedFromGroup(Member removedIndirectMember, Group group) {
        this.member = removedIndirectMember;
        this.group = group;
    }

    @Override
    public String toString() {
        return member + " was removed from "+ group +" totally.";
    }
}
