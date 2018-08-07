package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Service;

public class RequiredAttributeRemovedFromService extends AuditEvent {

	private final AttributeDefinition attributeDefinition;
	private final Service service;
	private final String message;

	public RequiredAttributeRemovedFromService(AttributeDefinition attribute, Service service) {
		this.attributeDefinition = attribute;
		this.service = service;
		this.message = String.format("%s removed from %s as required attribute.", attribute, service);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public AttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}

	public Service getService() {
		return service;
	}

	@Override
	public String toString() {
		return message;
	}
}
