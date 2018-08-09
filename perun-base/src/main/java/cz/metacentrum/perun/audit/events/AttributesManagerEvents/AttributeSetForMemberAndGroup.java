package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeSetForMemberAndGroup extends AuditEvent {

	private final Attribute attribute;
	private final Member member;
	private final Group group;
	private final String message;

	public AttributeSetForMemberAndGroup(Attribute attribute, Member member, Group group) {
		this.attribute = attribute;
		this.member = member;
		this.group = group;
		this.message = String.format("%s set for %s and %s.", attribute, member, group);
	}

	public Attribute getAttribute() {
		return attribute;
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
