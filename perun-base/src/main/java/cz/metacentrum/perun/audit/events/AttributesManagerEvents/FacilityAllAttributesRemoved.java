package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;

public class FacilityAllAttributesRemoved implements AuditEvent {
	private Facility facility;

	private String name = this.getClass().getName();
	private String message;

	public FacilityAllAttributesRemoved(Facility facility) {
		this.facility = facility;
	}

	public FacilityAllAttributesRemoved() {
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
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return String.format("All attributes removed for %s.", facility);
	}
}
