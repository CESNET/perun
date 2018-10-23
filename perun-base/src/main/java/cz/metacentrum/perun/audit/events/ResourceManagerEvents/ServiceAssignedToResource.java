package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;

public class ServiceAssignedToResource implements AuditEvent {

	private Service service;
	private Resource resource;
	private String name = this.getClass().getName();
	private String message;

	public ServiceAssignedToResource() {
	}

	public ServiceAssignedToResource(Service service, Resource resource) {
		this.service = service;
		this.resource = resource;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
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
		return service + " asigned to " + resource;
	}
}
