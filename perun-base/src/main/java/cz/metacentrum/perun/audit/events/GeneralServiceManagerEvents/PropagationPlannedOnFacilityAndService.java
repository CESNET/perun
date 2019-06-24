package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class PropagationPlannedOnFacilityAndService extends AuditEvent {

	private Facility facility;
	private Service service;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public PropagationPlannedOnFacilityAndService() {
	}

	public PropagationPlannedOnFacilityAndService(Facility facility, Service service) {
		this.facility = facility;
		this.service = service;
		this.message = formatMessage("propagation planned: On %s and %s.", facility, service);
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
