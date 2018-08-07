package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;

public class AllAttributesRemovedForResourceAndMember extends AuditEvent {

	private final Resource resource;
	private final Member member;
	private final String message;

	public AllAttributesRemovedForResourceAndMember(Resource resource, Member member) {
		this.resource = resource;
		this.member = member;
		this.message = String.format("All attributes removed for %s and %s.", resource, member);
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
