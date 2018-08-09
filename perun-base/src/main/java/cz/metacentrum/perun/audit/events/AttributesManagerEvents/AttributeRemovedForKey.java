package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForKey extends AuditEvent {

	private final AttributeDefinition attribute;
	private final String key;
	private final String message;

	public AttributeRemovedForKey(AttributeDefinition attribute, String key) {
		this.attribute = attribute;
		this.key = key;
		this.message = String.format("%s removed for %s.", attribute, key);
	}

	public AttributeDefinition getAttribute() {
		return attribute;
	}

	public String getKey() {
		return key;
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
