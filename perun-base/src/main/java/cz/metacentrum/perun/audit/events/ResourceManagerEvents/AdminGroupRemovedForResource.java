package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;

public class AdminGroupRemovedForResource implements AuditEvent {

	private Group group;
	private Resource resource;
	private String name = this.getClass().getName();
	private String message;

	public AdminGroupRemovedForResource(Group group, Resource resource) {
		this.group = group;
		this.resource = resource;
	}

	public AdminGroupRemovedForResource() {
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

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Group " + group + " was removed from admins of " + resource + ".";
	}
}
