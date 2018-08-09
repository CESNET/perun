package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class FreeAllDenialsOnFacility extends AuditEvent {

	private final Facility facility;
	private final String message;

	public FreeAllDenialsOnFacility(Facility facility) {
		this.facility = facility;
		this.message = String.format("free all denials: on %s.", facility);
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
