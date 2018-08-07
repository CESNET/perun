package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;

public class MemberSuspended extends AuditEvent {

	private final Member member;
	private final String engineForceKeyword;
	private final String message;

	public MemberSuspended(Member member, String engineForceKeyword) {
		this.member = member;
		this.engineForceKeyword = engineForceKeyword;
		this.message = String.format("%s suspended #%s.", member, engineForceKeyword);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Member getMember() {
		return member;
	}

	public String getEngineForceKeyword() {
		return engineForceKeyword;
	}

	@Override
	public String toString() {
		return message;
	}
}
