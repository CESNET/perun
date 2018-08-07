package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Host;

public class AllAttributesRemovedForHost extends AuditEvent {

	private final Host host;
	private final String message;

	public AllAttributesRemovedForHost(Host host) {
		this.host = host;
		this.message = String.format("All attributes removed for %s.", host);
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
