package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;

public class AdminGroupRemovedForResource extends AuditEvent implements EngineIgnoreEvent {

	private Group group;
	private Resource resource;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AdminGroupRemovedForResource() {
	}

	public AdminGroupRemovedForResource(Group group, Resource resource) {
		this.group = group;
		this.resource = resource;
		this.message = formatMessage("Group %s was removed from admins of %s.", group, resource);
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
