package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ServicesPackage;

public class ServicesPackageCreated extends AuditEvent {

	private final ServicesPackage servicesPackage;
	private final String message;

	public ServicesPackageCreated(ServicesPackage servicesPackage) {
		this.servicesPackage = servicesPackage;
		this.message = String.format("%s created.", servicesPackage);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public ServicesPackage getServicesPackage() {
		return servicesPackage;
	}

	@Override
	public String toString() {
		return message;
	}
}
