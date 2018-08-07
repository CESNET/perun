package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Service;

public class ServiceUpdated extends AuditEvent {

	private final Service service;
	private final String message;

	public ServiceUpdated(Service service) {
		this.service = service;
		this.message = String.format("%s updated.", service);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Service getService() {
		return service;
	}

	@Override
	public String toString() {
		return message;
	}
}
