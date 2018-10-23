package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;

public class SponsoredMemberSet extends AuditEvent {

	private Member sponsoredMember;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public SponsoredMemberSet() {
	}

	public SponsoredMemberSet(Member sponsoredMember) {
		this.sponsoredMember = sponsoredMember;
		this.message = formatMessage("%s is now sponsored.", sponsoredMember);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Member getSponsoredMember() {
		return sponsoredMember;
	}

	@Override
	public String toString() {
		return message;
	}
}
