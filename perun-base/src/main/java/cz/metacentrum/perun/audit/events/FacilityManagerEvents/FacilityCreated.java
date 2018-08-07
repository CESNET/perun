package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;


public class FacilityCreated extends AuditEvent {

	private final Facility facility;
	private final String message;

	public FacilityCreated(Facility facility) {
		this.facility = facility;
		this.message = String.format("Facility created: %s",facility);
	}

	public Facility getFacility() {
		return facility;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return message;
	}
}
