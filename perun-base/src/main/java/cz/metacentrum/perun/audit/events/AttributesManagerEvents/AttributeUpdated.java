package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;

public class AttributeUpdated extends AuditEvent {

	private final AttributeDefinition attributeDefinition;
	private final String message;

	public AttributeUpdated(AttributeDefinition attributeDefinition) {
		this.attributeDefinition = attributeDefinition;
		this.message = String.format("%s updated.", attributeDefinition);
	}

	public AttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
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
