package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Facility;


public class FacilityCreated extends AuditEvent implements EngineIgnoreEvent {

	private Facility facility;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public FacilityCreated() {
	}

	public FacilityCreated(Facility facility) {
		this.facility = facility;
		this.message = formatMessage("%s created.",facility);
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
