package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;

public class DestinationsRemovedFromAllServices extends AuditEvent {

	private final Facility facility;
	private final String message;

	public DestinationsRemovedFromAllServices(Facility facility) {
		this.facility = facility;
		this.message = String.format("All destinations removed from %s for all services.", facility);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Facility getFacility() {
		return facility;
	}

	@Override
	public String toString() {
		return message;
	}
}
