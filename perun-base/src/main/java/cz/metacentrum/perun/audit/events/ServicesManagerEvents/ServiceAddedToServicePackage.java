package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesPackage;

public class ServiceAddedToServicePackage extends AuditEvent {

	private final Service service;
	private final ServicesPackage servicesPackage;
	private final String message;

	public ServiceAddedToServicePackage(Service service, ServicesPackage servicesPackage) {
		this.service = service;
		this.servicesPackage = servicesPackage;
		this.message = String.format("%s added to %s.", service, servicesPackage);
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
