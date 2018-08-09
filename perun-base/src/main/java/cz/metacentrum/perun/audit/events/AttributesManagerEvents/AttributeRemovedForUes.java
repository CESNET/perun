package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.UserExtSource;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForUes extends AuditEvent {

	private final AttributeDefinition attribute;
	private final UserExtSource ues;
	private final String message;

	public AttributeRemovedForUes(AttributeDefinition attribute, UserExtSource ues) {
		this.attribute = attribute;
		this.ues = ues;
		this.message = String.format("%s removed for %s.", attribute, ues);
	}

	public AttributeDefinition getAttribute() {
		return attribute;
	}

	public UserExtSource getUes() {
		return ues;
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
