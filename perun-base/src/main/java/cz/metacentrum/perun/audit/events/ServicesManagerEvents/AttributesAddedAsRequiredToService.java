package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Service;

import java.util.List;

public class AttributesAddedAsRequiredToService extends AuditEvent {

	private final List<? extends AttributeDefinition> attributes;
	private final Service service;
	private final String message;

	public AttributesAddedAsRequiredToService(List<? extends AttributeDefinition> attributes, Service service) {
		this.attributes = attributes;
		this.service = service;
		this.message = String.format("%s added to %s as required attributes.", attributes, service);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public List<? extends AttributeDefinition> getAttributes() {
		return attributes;
	}

	public Service getService() {
		return service;
	}

	@Override
	public String toString() {
		return message;
	}
}
