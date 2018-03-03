package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Status;

public class MemberValidatedFailed {

    private Member member;
    private Status status;

    public MemberValidatedFailed(Member member, Status oldStatus) {
        this.member = member;
        this.status = oldStatus;
    }

    @Override
    public String toString() {
        return "Validation of " + member + " failed. He stays in " + status + " state.";
    }
}
