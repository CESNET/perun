package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesPackage;

public class ServiceRemovedFromServicesPackage extends AuditEvent {

	private final Service service;
	private final ServicesPackage servicesPackage;
	private final String message;

	public ServiceRemovedFromServicesPackage(Service service, ServicesPackage servicesPackage) {
		this.service = service;
		this.servicesPackage = servicesPackage;
		this.message = String.format("%s removed from %s.", service, servicesPackage);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Service getService() {
		return service;
	}

	public ServicesPackage getServicesPackage() {
		return servicesPackage;
	}

	@Override
	public String toString() {
		return message;
	}
}
