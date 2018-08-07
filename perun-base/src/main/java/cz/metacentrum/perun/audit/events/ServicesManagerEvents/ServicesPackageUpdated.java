package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ServicesPackage;

public class ServicesPackageUpdated extends AuditEvent {

	private final ServicesPackage servicesPackage;
	private final String message;

	public ServicesPackageUpdated(ServicesPackage servicesPackage) {
		this.servicesPackage = servicesPackage;
		this.message = String.format("%s updated.", servicesPackage);
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
