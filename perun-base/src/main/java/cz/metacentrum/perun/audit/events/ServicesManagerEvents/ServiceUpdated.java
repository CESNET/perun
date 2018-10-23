package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Service;

public class ServiceUpdated extends AuditEvent {

	private Service service;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public ServiceUpdated() {
	}

	public ServiceUpdated(Service service) {
		this.service = service;
		this.message = formatMessage("%s updated.", service);
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
