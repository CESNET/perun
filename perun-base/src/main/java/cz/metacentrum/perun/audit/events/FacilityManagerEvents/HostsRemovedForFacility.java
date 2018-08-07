package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Host;

import java.util.List;

public class HostsRemovedForFacility extends AuditEvent {

	private final List<Host> hosts;
	private final Facility facility;
	private final String message;

	public HostsRemovedForFacility(List<Host> hosts, Facility facility) {
		this.facility = facility;
		this.hosts = hosts;
		this.message = String.format("Hosts %s removed from cluster %s", hosts, facility);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public List<Host> getHosts() {
		return hosts;
	}

	public Facility getFacility() {
		return facility;
	}

	@Override
	public String toString() {
		return message;
	}
}
