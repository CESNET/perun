package cz.metacentrum.perun.audit.events.MembersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineForceEvent;
import cz.metacentrum.perun.core.api.Member;


/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class MemberUnsuspended extends AuditEvent implements EngineForceEvent {

	private Member member;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public MemberUnsuspended() { }

	public MemberUnsuspended(Member member) {
		this.member = member;
		this.message = formatMessage("%s unsuspended.", member);
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