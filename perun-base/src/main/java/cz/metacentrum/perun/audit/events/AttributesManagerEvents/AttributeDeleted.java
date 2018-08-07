package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;

public class AttributeDeleted extends AuditEvent {

	private final AttributeDefinition attributeDefinition;
	private final String message;

	public AttributeDeleted(AttributeDefinition attribute) {
		attributeDefinition = attribute;
		this.message = String.format("%s deleted.", attributeDefinition);
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
