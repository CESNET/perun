package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Resource;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForResource extends AuditEvent {

	private final AttributeDefinition attribute;
	private final Resource resource;
	private final String message;

	public AttributeRemovedForResource(AttributeDefinition attribute, Resource resource) {
		this.attribute = attribute;
		this.resource = resource;
		this.message = String.format("%s removed for %s.", attribute, resource);
	}

	public AttributeDefinition getAttribute() {
		return attribute;
	}

	public Resource getResource() {
		return resource;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return message;
	}
}
