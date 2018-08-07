package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Host;

public class HostAddedToFacility extends AuditEvent {

	private final Host host;
	private final Facility facility;
	private final String message;

	public HostAddedToFacility(Host host, Facility facility) {
		this.facility = facility;
		this.host = host;
		this.message = String.format("%s added to facility %s", host, facility);
	}

	public Host getHost() {
		return host;
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
