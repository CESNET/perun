package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesPackage;

public class ServiceAddedToServicePackage implements AuditEvent {

	private Service service;
	private ServicesPackage servicesPackage;
	private String name = this.getClass().getName();
	private String message;

	public ServiceAddedToServicePackage() {
	}

	public ServiceAddedToServicePackage(Service service, ServicesPackage servicesPackage) {
		this.service = service;
		this.servicesPackage = servicesPackage;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public ServicesPackage getServicesPackage() {
		return servicesPackage;
	}

	public void setServicesPackage(ServicesPackage servicesPackage) {
		this.servicesPackage = servicesPackage;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return service + " added to " + servicesPackage + ".";
	}
}
