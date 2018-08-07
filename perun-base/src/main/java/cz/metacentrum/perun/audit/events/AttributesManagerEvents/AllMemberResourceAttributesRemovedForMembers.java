package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Resource;

public class AllMemberResourceAttributesRemovedForMembers extends AuditEvent {

	private final Resource resource;
	private final String message;

	public AllMemberResourceAttributesRemovedForMembers(Resource resource) {
		this.resource = resource;
		this.message = String.format("All non-virtual member-resource attributes removed for all members and %s.", resource);
	}

	public Resource getResource() {
		return resource;
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
