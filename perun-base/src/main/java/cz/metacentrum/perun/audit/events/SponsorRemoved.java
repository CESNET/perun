package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;

public class SponsorRemoved {

    private Member sponsoredMember;
    private User sponsor;

    public SponsorRemoved(Member sponsoredMember, User sponsorToRemove) {
        this.sponsoredMember = sponsoredMember;
        sponsor = sponsorToRemove;
    }

    @Override
    public String toString() {
        return "Sponsorship of " + sponsoredMember + " by " + sponsor + " canceled.";
    }
}
