package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineForceEvent;
import cz.metacentrum.perun.core.api.Member;

public class MemberUnsuspended extends AuditEvent implements EngineForceEvent {

	private Member member;
	private String engineForceKeyword;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public MemberUnsuspended() {
	}

	public MemberUnsuspended(Member member, String engineForceKeyword) {
		this.member = member;
		this.engineForceKeyword = engineForceKeyword;
		this.message = formatMessage("%s unsuspended #%s.", member, engineForceKeyword);
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
