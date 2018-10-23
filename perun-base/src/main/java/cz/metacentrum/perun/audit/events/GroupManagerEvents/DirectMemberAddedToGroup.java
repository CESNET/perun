package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

public class DirectMemberAddedToGroup implements AuditEvent {

	private Group group;
	private Member member;
	private String name = this.getClass().getName();
	private String message;

	public DirectMemberAddedToGroup(Member member, Group group) {
		this.group = group;
		this.member = member;
	}

	public DirectMemberAddedToGroup() {
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return member + " added to " + group + ".";
	}
}

