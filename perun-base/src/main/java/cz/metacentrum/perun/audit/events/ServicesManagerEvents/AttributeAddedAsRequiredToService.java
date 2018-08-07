package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Service;

public class AttributeAddedAsRequiredToService extends AuditEvent {

	private final AttributeDefinition attribute;
	private final Service service;
	private final String message;

	public AttributeAddedAsRequiredToService(AttributeDefinition attribute, Service service) {
		this.attribute = attribute;
		this.service = service;
		this.message = String.format("%s added to %s as required attribute.", attribute, service);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public AttributeDefinition getAttribute() {
		return attribute;
	}

	public Service getService() {
		return service;
	}

	@Override
	public String toString() {
		return message;
	}
}
