package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;

public class SponsorRemoved extends AuditEvent {

	private Member sponsoredMember;
	private User sponsor;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public SponsorRemoved() {
	}

	public SponsorRemoved(Member sponsoredMember, User sponsorToRemove) {
		this.sponsoredMember = sponsoredMember;
		this.sponsor = sponsorToRemove;
		this.message = formatMessage("Sponsorship of %s by %s canceled.", sponsoredMember, sponsor);
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
