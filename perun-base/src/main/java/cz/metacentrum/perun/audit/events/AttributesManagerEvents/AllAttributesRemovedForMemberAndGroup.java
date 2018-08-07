package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

public class AllAttributesRemovedForMemberAndGroup extends AuditEvent {

	private final Member member;
	private final Group group;
	private final String message;


	public AllAttributesRemovedForMemberAndGroup(Member member, Group group) {
		this.member = member;
		this.group = group;
		this.message = String.format("All attributes removed for %s and %s.", member, group);
	}

	public Member getMember() {
		return member;
	}

	public Group getGroup() {
		return group;
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
