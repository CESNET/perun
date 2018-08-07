package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

public class IndirectMemberRemovedFromGroup extends AuditEvent {

	private final Member member;
	private final Group group;
	private final String message;

	public IndirectMemberRemovedFromGroup(Member removedIndirectMember, Group group) {
		this.member = removedIndirectMember;
		this.group = group;
		this.message = String.format("%s was removed from %s totally.", member, group);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Member getMember() {
		return member;
	}

	public Group getGroup() {
		return group;
	}

	@Override
	public String toString() {
		return message;
	}
}
