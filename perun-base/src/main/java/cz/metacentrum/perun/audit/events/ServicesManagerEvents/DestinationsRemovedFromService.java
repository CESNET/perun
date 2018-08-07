package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;

public class DestinationsRemovedFromService extends AuditEvent {

	private final Facility facility;
	private final Service service;
	private final String message;

	public DestinationsRemovedFromService(Service service, Facility facility) {
		this.facility = facility;
		this.service = service;
		this.message = String.format("All destinations removed from %s and %s.", service, facility);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Facility getFacility() {
		return facility;
	}

	public Service getService() {
		return service;
	}

	@Override
	public String toString() {
		return message;
	}
}
