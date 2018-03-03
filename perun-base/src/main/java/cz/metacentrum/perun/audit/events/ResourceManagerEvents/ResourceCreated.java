package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Resource;

public class ResourceCreated implements AuditEvent {

	private Resource resource;
	private String name = this.getClass().getName();
	private String message;

	public ResourceCreated() {
	}

	public ResourceCreated(Resource resource) {
		this.resource = resource;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
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
		return resource + " created.";
	}
}
