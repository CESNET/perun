package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;

public class DestinationsRemovedFromAllServices implements AuditEvent {

	private Facility facility;
	private String name = this.getClass().getName();
	private String message;

	public DestinationsRemovedFromAllServices() {
	}

	public DestinationsRemovedFromAllServices(Facility facility) {
		this.facility = facility;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "All destinations removed from " + facility + " for all services.";
	}
}
