package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Member;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeSetForMember extends AuditEvent {

	private final Attribute attribute;
	private final Member member;
	private final String message;

	public AttributeSetForMember(Attribute attribute, Member member) {
		this.attribute = attribute;
		this.member = member;
		this.message = String.format("%s set for %s.", attribute, member);
	}

	public Attribute getAttribute() {
		return attribute;
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
