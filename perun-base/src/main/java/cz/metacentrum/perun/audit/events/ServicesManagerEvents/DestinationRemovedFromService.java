package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;

public class DestinationRemovedFromService extends AuditEvent {

	private final Destination destination;
	private final Service service;
	private final Facility facility;
	private final String message;

	public DestinationRemovedFromService(Destination destination, Service service, Facility facility) {
		this.destination = destination;
		this.facility = facility;
		this.service = service;
		this.message = String.format("%s removed from %s and %s.", destination, service, facility);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Destination getDestination() {
		return destination;
	}

	public Service getService() {
		return service;
	}

	public Facility getFacility() {
		return facility;
	}

	@Override
	public String toString() {
		return message;
	}
}
