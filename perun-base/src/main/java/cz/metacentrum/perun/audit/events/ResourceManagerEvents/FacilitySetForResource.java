package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;

public class FacilitySetForResource implements AuditEvent {
	private Facility facility;
	private Resource resource;
	private String name = this.getClass().getName();
	private String message;

	public FacilitySetForResource() {
	}

	public FacilitySetForResource(Facility facility, Resource resource) {
		this.facility = facility;
		this.resource = resource;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
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
		return facility + " set for " + resource;
	}
}
