package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;

public class AttributeChangedForResourceAndMember extends AuditEvent {

	private Attribute attribute;
	private Resource resource;
	private Member member;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AttributeChangedForResourceAndMember() {
	}

	public AttributeChangedForResourceAndMember(Attribute attribute, Resource resource, Member member) {
		this.attribute = attribute;
		this.resource = resource;
		this.member = member;
		this.message = formatMessage("%s changed for %s and %s.", attribute, resource, member);
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public Resource getResource() {
		return resource;
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
