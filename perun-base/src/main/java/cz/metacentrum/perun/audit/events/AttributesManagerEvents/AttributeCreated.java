package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;

public class AttributeCreated extends AuditEvent implements EngineIgnoreEvent {

	private AttributeDefinition attribute;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AttributeCreated() {
	}

	public AttributeCreated(AttributeDefinition attribute) {
		this.attribute = attribute;
		this.message = formatMessage("%s created.", attribute);
	}

	public AttributeDefinition getAttribute() {
		return attribute;
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
