package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Host;

public class HostRemovedForFacility extends AuditEvent {

	private Host host;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public HostRemovedForFacility() {
	}

	public HostRemovedForFacility(Host host) {
		this.host = host;
		this.message = formatMessage("%s removed.", host);
	}

	public Host getHost() {
		return host;
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
