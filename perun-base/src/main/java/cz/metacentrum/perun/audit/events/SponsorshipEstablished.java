package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;

public class SponsorshipEstablished {

    private Member sponsoredMember;
    private User sponsor;

    public SponsorshipEstablished(Member sponsoredMember, User sponsor) {
        this.sponsoredMember = sponsoredMember;
        this.sponsor = sponsor;
    }

    @Override
    public String toString() {
        return "Sponsorship of "+ sponsoredMember +" by "+ sponsor +" established.";
    }
}
