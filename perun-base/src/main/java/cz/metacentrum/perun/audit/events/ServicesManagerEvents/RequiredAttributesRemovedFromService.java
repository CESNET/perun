package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Service;

import java.util.List;

public class RequiredAttributesRemovedFromService extends AuditEvent {

	private final Service service;
	private final List<? extends AttributeDefinition> attributes;
	private final String message;

	public RequiredAttributesRemovedFromService(List<? extends AttributeDefinition> attributes, Service service) {
		this.service = service;
		this.attributes = attributes;
		this.message = String.format("%s removed from %s as required attributes.", attributes, service);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Service getService() {
		return service;
	}

	public List<? extends AttributeDefinition> getAttributes() {
		return attributes;
	}

	@Override
	public String toString() {
		return message;
	}
}
