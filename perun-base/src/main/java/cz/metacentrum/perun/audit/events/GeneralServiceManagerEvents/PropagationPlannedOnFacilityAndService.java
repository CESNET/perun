package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class PropagationPlannedOnFacilityAndService extends AuditEvent {

	private final Facility facility;
	private final Service service;
	private final String message;

	public PropagationPlannedOnFacilityAndService(Facility facility, Service service) {
		this.facility = facility;
		this.service = service;
		this.message = String.format("propagation planned: On %s.", service);
	}

	public Facility getFacility() {
		return facility;
	}

	public Service getService() {
		return service;
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
