package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;

public class AllAttributesRemovedForGroupAndResource extends AuditEvent {

	private final Group group;
	private final Resource resource;
	private final String message;

	public AllAttributesRemovedForGroupAndResource(Group group, Resource resource) {
		this.group = group;
		this.resource = resource;
		this.message = String.format("All attributes removed for %s and %s.", group, resource);
	}

	public Group getGroup() {
		return group;
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
