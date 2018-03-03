package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Service;

public class ServicePropagationPlanedOnService implements AuditEvent {

	private String propagationPlanned;
	private Service service;
	private String name = this.getClass().getName();
	private String message;

	public ServicePropagationPlanedOnService(String propagationPlanned, Service service) {
		this.propagationPlanned = propagationPlanned;
		this.service = service;
	}

	public ServicePropagationPlanedOnService() {
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPropagationPlanned() {
		return propagationPlanned;
	}

	public void setPropagationPlanned(String propagationPlanned) {
		this.propagationPlanned = propagationPlanned;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return propagationPlanned + " On " + service;
	}
}
