package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;

public class SponsorRemoved extends AuditEvent {

	private final Member sponsoredMember;
	private final User sponsor;
	private final String message;

	public SponsorRemoved(Member sponsoredMember, User sponsorToRemove) {
		this.sponsoredMember = sponsoredMember;
		this.sponsor = sponsorToRemove;
		this.message = String.format("Sponsorship of %s by %s canceled.", sponsoredMember, sponsor);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Member getSponsoredMember() {
		return sponsoredMember;
	}

	public User getSponsor() {
		return sponsor;
	}

	@Override
	public String toString() {
		return message;
	}
}
