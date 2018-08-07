package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Resource;

public class ResourceCreated extends AuditEvent {

	private final Resource resource;
	private final String message;

	public ResourceCreated(Resource resource) {
		this.resource = resource;
		this.message = String.format("%s created.", resource);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Resource getResource() {
		return resource;
	}

	@Override
	public String toString() {
		return message;
	}
}
