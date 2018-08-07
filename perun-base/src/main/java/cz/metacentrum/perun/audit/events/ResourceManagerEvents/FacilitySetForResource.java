package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;

public class FacilitySetForResource extends AuditEvent {

	private final Facility facility;
	private final Resource resource;
	private final String message;

	public FacilitySetForResource(Facility facility, Resource resource) {
		this.facility = facility;
		this.resource = resource;
		this.message = String.format("%s set for %s", facility, resource);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Facility getFacility() {
		return facility;
	}

	public Resource getResource() {
		return resource;
	}

	@Override
	public String toString() {
		return facility + " set for " + resource;
	}
}
