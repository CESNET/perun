package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

public class DirectMemberAddedToGroup extends AuditEvent {

	private final Group group;
	private final Member member;
	private final String message;

	public DirectMemberAddedToGroup(Member member, Group group) {
		this.group = group;
		this.member = member;
		this.message = String.format("%s added to %s.", member, group);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Group getGroup() {
		return group;
	}

	public Member getMember() {
		return member;
	}

	@Override
	public String toString() {
		return message;
	}
}

