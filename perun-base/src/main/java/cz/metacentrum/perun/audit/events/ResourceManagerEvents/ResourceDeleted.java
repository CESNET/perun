package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;

import java.util.List;

public class ResourceDeleted extends AuditEvent {

	private Resource resource;
	private Facility facility;
	private List<Service> services;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public ResourceDeleted() {
	}

	public ResourceDeleted(Resource resource, Facility facility, List<Service> services) {
		this.resource = resource;
		this.facility = facility;
		this.services = services;
		this.message = formatMessage("%s deleted.#%s. Afected services:%s.", resource, facility, services);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Resource getResource() {
		return resource;
	}

	public Facility getFacility() {
		return facility;
	}

	public List<Service> getServices() {
		return services;
	}

	@Override
	public String toString() {
		return message;
	}
}
