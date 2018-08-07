package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Service;

public class ServiceCreated extends AuditEvent {

	private final Service service;
	private final String message;

	public ServiceCreated(Service service) {
		this.service = service;
		this.message = String.format("%s created.", service);
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
