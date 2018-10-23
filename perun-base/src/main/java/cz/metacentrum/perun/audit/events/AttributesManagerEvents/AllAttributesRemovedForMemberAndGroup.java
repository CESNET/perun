package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

public class AllAttributesRemovedForMemberAndGroup implements AuditEvent {

	private Member member;
	private Group group;
	private String name = this.getClass().getName();
	private String message;


	public AllAttributesRemovedForMemberAndGroup(Member member, Group group) {
		this.member = member;
		this.group = group;
	}

	public AllAttributesRemovedForMemberAndGroup() {

	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return String.format("All attributes removed for %s and %s.", member, group);
	}
}
