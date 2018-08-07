package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;

public class ServiceRemovedFromResource extends AuditEvent {

	private final Service service;
	private final Resource resource;
	private final String message;

	public ServiceRemovedFromResource(Service service, Resource resource) {
		this.service = service;
		this.resource = resource;
		this.message = String.format("%s removed from %s", service, resource);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Service getService() {
		return service;
	}

	public Resource getResource() {
		return resource;
	}

	@Override
	public String toString() {
		return message;
	}
}
