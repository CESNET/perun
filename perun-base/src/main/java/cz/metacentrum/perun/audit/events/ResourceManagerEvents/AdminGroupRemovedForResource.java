package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;

public class AdminGroupRemovedForResource extends AuditEvent {

	private final Group group;
	private final Resource resource;
	private final String message;

	public AdminGroupRemovedForResource(Group group, Resource resource) {
		this.group = group;
		this.resource = resource;
		this.message = String.format("Group %s was removed from admins of %s.", group, resource);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Group getGroup() {
		return group;
	}

	public Resource getResource() {
		return resource;
	}

	@Override
	public String toString() {
		return message;
	}
}
