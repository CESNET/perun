package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Host;

public class HostRemovedForFacility implements AuditEvent {
	private Host host;

	private String name = this.getClass().getName();
	private String message;

	public HostRemovedForFacility() {
	}

	public HostRemovedForFacility(Host host) {
		this.host = host;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
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
		return String.format("%s removed.", host);

	}
}
