package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;

public class AllUserFacilityAttributesRemoved extends AuditEvent {

	private final Facility facility;
	private final String message;

	public AllUserFacilityAttributesRemoved(Facility facility) {
		this.facility = facility;
		this.message = String.format("All user-facility attributes removed for %s for any user.", facility);
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
