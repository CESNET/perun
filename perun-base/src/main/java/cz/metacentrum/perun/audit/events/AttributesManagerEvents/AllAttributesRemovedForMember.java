package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;

public class AllAttributesRemovedForMember extends AuditEvent {

	private Member member;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AllAttributesRemovedForMember() {
	}

	public AllAttributesRemovedForMember(Member member) {
		this.member = member;
		this.message = formatMessage("All attributes removed for %s.", member);
	}

	public Member getMember() {
		return member;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return message;
	}
}
