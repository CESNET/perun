package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;

public class SponsorshipEstablished implements AuditEvent {

	private Member sponsoredMember;
	private User sponsor;
	private String name = this.getClass().getName();
	private String message;

	public SponsorshipEstablished(Member sponsoredMember, User sponsor) {
		this.sponsoredMember = sponsoredMember;
		this.sponsor = sponsor;
	}

	public SponsorshipEstablished() {
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Member getSponsoredMember() {
		return sponsoredMember;
	}

	public void setSponsoredMember(Member sponsoredMember) {
		this.sponsoredMember = sponsoredMember;
	}

	public User getSponsor() {
		return sponsor;
	}

	public void setSponsor(User sponsor) {
		this.sponsor = sponsor;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Sponsorship of " + sponsoredMember + " by " + sponsor + " established.";
	}
}
