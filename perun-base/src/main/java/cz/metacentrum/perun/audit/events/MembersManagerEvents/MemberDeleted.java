package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;

public class MemberDeleted extends AuditEvent {

	private final Member member;
	private final String message;

	public MemberDeleted(Member member) {
		this.member = member;
		this.message = String.format("%s deleted.", member);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Member getMember() {
		return member;
	}

	@Override
	public String toString() {
		return message;
	}
}
