package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;

public class AttributeAuthzDeleted extends AuditEvent {

	private final AttributeDefinition attributeDefinition;
	private final String message;

	public AttributeAuthzDeleted(AttributeDefinition attribute) {
		attributeDefinition = attribute;
		this.message = String.format("All authorization information were deleted for %s.", attributeDefinition);
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
