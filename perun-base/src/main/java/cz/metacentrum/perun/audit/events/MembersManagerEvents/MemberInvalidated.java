package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;

public class MemberInvalidated extends AuditEvent {

	private final Member member;
	private final String message;

	public MemberInvalidated(Member member) {
		this.member = member;
		this.message = String.format("%s invalidated.", member);
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
