package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;

public class FacilityAllAttributesRemoved extends AuditEvent {

	private final Facility facility;
	private final String message;

	public FacilityAllAttributesRemoved(Facility facility) {
		this.facility = facility;
		this.message = String.format("All attributes removed for %s.", facility);
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
